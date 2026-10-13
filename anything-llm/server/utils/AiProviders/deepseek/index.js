const { NativeEmbedder } = require("../../EmbeddingEngines/native");
const {
  LLMPerformanceMonitor,
} = require("../../helpers/chat/LLMPerformanceMonitor");
const { v4: uuidv4 } = require("uuid");
const { MODEL_MAP } = require("../modelMap");
const {
  writeResponseChunk,
  clientAbortedHandler,
} = require("../../helpers/chat/responses");

class DeepSeekLLM {
  constructor(embedder = null, modelPreference = null) {
    if (!process.env.DEEPSEEK_API_KEY)
      throw new Error("No DeepSeek API key was set.");
    this.className = "DeepSeekLLM";
    const { OpenAI: OpenAIApi } = require("openai");

    this.openai = new OpenAIApi({
      apiKey: process.env.DEEPSEEK_API_KEY,
      baseURL: "https://api.deepseek.com/v1",
    });
    this.model =
      modelPreference || process.env.DEEPSEEK_MODEL_PREF || "deepseek-chat";
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
    return MODEL_MAP.get("deepseek", modelName) ?? 8192;
  }

  promptWindowLimit() {
    return MODEL_MAP.get("deepseek", this.model) ?? 8192;
  }

  async isValidChatCompletionModel(modelName = "") {
    const models = await this.openai.models.list().catch(() => ({ data: [] }));
    return models.data.some((model) => model.id === modelName);
  }

  constructPrompt({
    systemPrompt = "",
    contextTexts = [],
    chatHistory = [],
    userPrompt = "",
  }) {
    const prompt = {
      role: "system",
      content: `${systemPrompt}${this.#appendContext(contextTexts)}`,
    };
    return [prompt, ...chatHistory, { role: "user", content: userPrompt }];
  }

  /**
   * Parses and prepends reasoning from the response and returns the full text response.
   * @param {Object} response
   * @returns {string}
   */
  #parseReasoningFromResponse({ message }) {
    let textResponse = message?.content;
    if (
      !!message?.reasoning_content &&
      message.reasoning_content.trim().length > 0
    )
      textResponse = `<think>${message.reasoning_content}</think>${textResponse}`;
    return textResponse;
  }

  async getChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `DeepSeek chat: ${this.model} is not valid for chat completion!`
      );

    const result = await LLMPerformanceMonitor.measureAsyncFunction(
      this.openai.chat.completions
        .create({
          model: this.model,
          messages,
          temperature,
        })
        .catch((e) => {
          throw new Error(e.message);
        })
    );

    if (
      !result?.output?.hasOwnProperty("choices") ||
      result?.output?.choices?.length === 0
    )
      throw new Error(
        `Invalid response body returned from DeepSeek: ${JSON.stringify(result.output)}`
      );

    return {
      textResponse: this.#parseReasoningFromResponse(result.output.choices[0]),
      metrics: {
        prompt_tokens: result.output.usage.prompt_tokens || 0,
        completion_tokens: result.output.usage.completion_tokens || 0,
        total_tokens: result.output.usage.total_tokens || 0,
        outputTps: result.output.usage.completion_tokens / result.duration,
        duration: result.duration,
        model: this.model,
        timestamp: new Date(),
      },
    };
  }

  async streamGetChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `DeepSeek chat: ${this.model} is not valid for chat completion!`
      );

    const measuredStreamRequest = await LLMPerformanceMonitor.measureStream({
      func: this.openai.chat.completions.create({
        model: this.model,
        stream: true,
        messages,
        temperature,
      }),
      messages,
      runPromptTokenCalculation: false,
      modelTag: this.model,
    });

    return measuredStreamRequest;
  }

  // TODO: This is a copy of the generic handleStream function in responses.js
  handleStream(response, stream, responseProps) {
    const { uuid = uuidv4(), sources = [] } = responseProps;
    let hasUsageMetrics = false;
    let usage = {
      completion_tokens: 0,
    };

    return new Promise(async (resolve) => {
      let fullText = "";
      let reasoningText = "";

      // Establish listener to early-abort a streaming response
      const handleAbort = () => {
        stream?.endMeasurement(usage);
        clientAbortedHandler(resolve, fullText);
      };
      response.on("close", handleAbort);

      try {
        for await (const chunk of stream) {
          const message = chunk?.choices?.[0];
          const token = message?.delta?.content;
          const reasoningToken = message?.delta?.reasoning_content;

          if (
            chunk.hasOwnProperty("usage") && // exists
            !!chunk.usage && // is not null
            Object.values(chunk.usage).length > 0 // has values
          ) {
            if (chunk.usage.hasOwnProperty("prompt_tokens")) {
              usage.prompt_tokens = Number(chunk.usage.prompt_tokens);
            }

            if (chunk.usage.hasOwnProperty("completion_tokens")) {
              hasUsageMetrics = true; // to stop estimating counter
              usage.completion_tokens = Number(chunk.usage.completion_tokens);
            }
          }

          if (reasoningToken) {
            if (reasoningText.length === 0) {
              writeResponseChunk(response, {
                uuid,
                sources: [],
                type: "textResponseChunk",
                textResponse: `<think>${reasoningToken}`,
                close: false,
                error: false,
              });
              reasoningText += `<think>${reasoningToken}`;
              continue;
            } else {
              writeResponseChunk(response, {
                uuid,
                sources: [],
                type: "textResponseChunk",
                textResponse: reasoningToken,
                close: false,
                error: false,
              });
              reasoningText += reasoningToken;
            }
          }

          if (!!reasoningText && !reasoningToken && token) {
            writeResponseChunk(response, {
              uuid,
              sources: [],
              type: "textResponseChunk",
              textResponse: `</think>`,
              close: false,
              error: false,
            });
            fullText += `${reasoningText}</think>`;
            reasoningText = "";
          }

          if (token) {
            fullText += token;
            // If we never saw a usage metric, we can estimate them by number of completion chunks
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

          if (
            message?.hasOwnProperty("finish_reason") && // Got valid message and it is an object with finish_reason
            message.finish_reason !== "" &&
            message.finish_reason !== null
          ) {
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
            break; // Break streaming when a valid finish_reason is first encountered
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
        resolve(fullText); // Return what we currently have - if anything.
      }
    });
  }

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
  DeepSeekLLM,
};
