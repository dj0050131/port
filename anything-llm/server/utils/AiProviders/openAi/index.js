const { v4: uuidv4 } = require("uuid");
const { NativeEmbedder } = require("../../EmbeddingEngines/native");
const {
  formatChatHistory,
  writeResponseChunk,
  clientAbortedHandler,
} = require("../../helpers/chat/responses");
const { MODEL_MAP } = require("../modelMap");
const {
  LLMPerformanceMonitor,
} = require("../../helpers/chat/LLMPerformanceMonitor");

class OpenAiLLM {
  constructor(embedder = null, modelPreference = null) {
    if (!process.env.OPEN_AI_KEY) throw new Error("No OpenAI API key was set.");
    this.className = "OpenAiLLM";
    const { OpenAI: OpenAIApi } = require("openai");

    this.openai = new OpenAIApi({
      apiKey: process.env.OPEN_AI_KEY,
    });
    this.model = modelPreference || process.env.OPEN_MODEL_PREF || "gpt-4o";
    this.limits = {
      history: this.promptWindowLimit() * 0.15,
      system: this.promptWindowLimit() * 0.15,
      user: this.promptWindowLimit() * 0.7,
    };

    this.embedder = embedder ?? new NativeEmbedder();
    this.defaultTemp = 0.7;
    this.log(
      `Initialized ${this.model} with context window ${this.promptWindowLimit()}`
    );
  }

  log(text, ...args) {
  }

  #appendContext(contextTexts = []) {
    if (!contextTexts || !contextTexts.length) return "";
    return (
      "\nContext:\n" +
      contextTexts
        .map((text, i) => {
          return `[CONTEXT ${i}]:\n${text}\n[END CONTEXT ${i}]\n\n`;
        })
        .join("")
    );
  }

  streamingEnabled() {
    return "streamGetChatCompletion" in this;
  }

  static promptWindowLimit(modelName) {
    return MODEL_MAP.get("openai", modelName) ?? 4_096;
  }

  promptWindowLimit() {
    return MODEL_MAP.get("openai", this.model) ?? 4_096;
  }

  // Short circuit if name has 'gpt' since we now fetch models from OpenAI API
  async isValidChatCompletionModel(modelName = "") {
    const isPreset =
      modelName.toLowerCase().includes("gpt") ||
      modelName.toLowerCase().startsWith("o");
    if (isPreset) return true;

    const model = await this.openai.models
      .retrieve(modelName)
      .then((modelObj) => modelObj)
      .catch(() => null);
    return !!model;
  }

  
  #generateContent({ userPrompt, attachments = [] }) {
    if (!attachments.length) {
      return userPrompt;
    }

    const content = [{ type: "input_text", text: userPrompt }];
    for (let attachment of attachments) {
      content.push({
        type: "input_image",
        image_url: attachment.contentString,
      });
    }
    return content.flat();
  }

  
  constructPrompt({
    systemPrompt = "",
    contextTexts = [],
    chatHistory = [],
    userPrompt = "",
    attachments = [], // This is the specific attachment for only this prompt
  }) {
    const prompt = {
      role: "system",
      content: `${systemPrompt}${this.#appendContext(contextTexts)}`,
    };
    return [
      prompt,
      ...formatChatHistory(chatHistory, this.#generateContent),
      {
        role: "user",
        content: this.#generateContent({ userPrompt, attachments }),
      },
    ];
  }

  
  #temperature(modelName, temperature) {
    const NO_TEMP_MODELS = ["o", "gpt-5"];

    if (NO_TEMP_MODELS.some((prefix) => modelName.startsWith(prefix))) {
      return 1;
    }

    return temperature;
  }

  async getChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `OpenAI chat: ${this.model} is not valid for chat completion!`
      );

    const result = await LLMPerformanceMonitor.measureAsyncFunction(
      this.openai.responses
        .create({
          model: this.model,
          input: messages,
          store: false,
          temperature: this.#temperature(this.model, temperature),
        })
        .catch((e) => {
          throw new Error(e.message);
        })
    );

    if (!result.output.hasOwnProperty("output_text")) return null;

    const usage = result.output.usage || {};
    return {
      textResponse: result.output.output_text,
      metrics: {
        prompt_tokens: usage.input_tokens || 0,
        completion_tokens: usage.output_tokens || 0,
        total_tokens: usage.total_tokens || 0,
        outputTps: usage.output_tokens
          ? usage.output_tokens / result.duration
          : 0,
        duration: result.duration,
        model: this.model,
        timestamp: new Date(),
      },
    };
  }

  async streamGetChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `OpenAI chat: ${this.model} is not valid for chat completion!`
      );

    const measuredStreamRequest = await LLMPerformanceMonitor.measureStream({
      func: this.openai.responses.create({
        model: this.model,
        stream: true,
        input: messages,
        store: false,
        temperature: this.#temperature(this.model, temperature),
      }),
      messages,
      runPromptTokenCalculation: false,
      modelTag: this.model,
    });

    return measuredStreamRequest;
  }

  handleStream(response, stream, responseProps) {
    const { uuid = uuidv4(), sources = [] } = responseProps;

    let hasUsageMetrics = false;
    let usage = {
      completion_tokens: 0,
    };

    return new Promise(async (resolve) => {
      let fullText = "";

      const handleAbort = () => {
        stream?.endMeasurement(usage);
        clientAbortedHandler(resolve, fullText);
      };
      response.on("close", handleAbort);

      try {
        for await (const chunk of stream) {
          if (chunk.type === "response.output_text.delta") {
            const token = chunk.delta;
            if (token) {
              fullText += token;
              if (!hasUsageMetrics) usage.completion_tokens++;

              writeResponseChunk(response, {
                uuid,
                sources: [],
                type: "textResponseChunk",
                textResponse: token,
                close: false,
                error: false,
              });
            }
          } else if (chunk.type === "response.completed") {
            const { response: res } = chunk;
            if (res.hasOwnProperty("usage") && !!res.usage) {
              hasUsageMetrics = true;
              usage = {
                ...usage,
                prompt_tokens: res.usage?.input_tokens || 0,
                completion_tokens: res.usage?.output_tokens || 0,
                total_tokens: res.usage?.total_tokens || 0,
              };
            }

            writeResponseChunk(response, {
              uuid,
              sources,
              type: "textResponseChunk",
              textResponse: "",
              close: true,
              error: false,
            });
            response.removeListener("close", handleAbort);
            stream?.endMeasurement(usage);
            resolve(fullText);
            break;
          }
        }
      } catch (e) {
        writeResponseChunk(response, {
          uuid,
          type: "abort",
          textResponse: null,
          sources: [],
          close: true,
          error: e.message,
        });
        stream?.endMeasurement(usage);
        resolve(fullText);
      }
    });
  }

  // Simple wrapper for dynamic embedder & normalize interface for all LLM implementations
  async embedTextInput(textInput) {
    return await this.embedder.embedTextInput(textInput);
  }
  async embedChunks(textChunks = []) {
    return await this.embedder.embedChunks(textChunks);
  }

  async compressMessages(promptArgs = {}, rawHistory = []) {
    const { messageArrayCompressor } = require("../../helpers/chat");
    const messageArray = this.constructPrompt(promptArgs);
    return await messageArrayCompressor(this, messageArray, rawHistory);
  }
}

module.exports = {
  OpenAiLLM,
};
