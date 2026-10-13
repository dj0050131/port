const { NativeEmbedder } = require("../../EmbeddingEngines/native");
const {
  LLMPerformanceMonitor,
} = require("../../helpers/chat/LLMPerformanceMonitor");
const {
  handleDefaultStreamResponseV2,
} = require("../../helpers/chat/responses");
const { MODEL_MAP } = require("../modelMap");

class GroqLLM {
  constructor(embedder = null, modelPreference = null) {
    const { OpenAI: OpenAIApi } = require("openai");
    if (!process.env.GROQ_API_KEY) throw new Error("No Groq API key was set.");

    this.openai = new OpenAIApi({
      baseURL: "https://api.groq.com/openai/v1",
      apiKey: process.env.GROQ_API_KEY,
    });
    this.model =
      modelPreference || process.env.GROQ_MODEL_PREF || "llama-3.1-8b-instant";
    this.limits = {
      history: this.promptWindowLimit() * 0.15,
      system: this.promptWindowLimit() * 0.15,
      user: this.promptWindowLimit() * 0.7,
    };

    this.embedder = embedder ?? new NativeEmbedder();
    this.defaultTemp = 0.7;
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

  #log(text, ...args) {
  }

  streamingEnabled() {
    return "streamGetChatCompletion" in this;
  }

  static promptWindowLimit(modelName) {
    return MODEL_MAP.get("groq", modelName) ?? 8192;
  }

  promptWindowLimit() {
    return MODEL_MAP.get("groq", this.model) ?? 8192;
  }

  async isValidChatCompletionModel(modelName = "") {
    return !!modelName; // name just needs to exist
  }

  
  #generateContent({ userPrompt, attachments = [] }) {
    if (!attachments.length) return userPrompt;

    const content = [{ type: "text", text: userPrompt }];
    for (let attachment of attachments) {
      content.push({
        type: "image_url",
        image_url: {
          url: attachment.contentString,
        },
      });
    }
    return content.flat();
  }

  
  #conditionalPromptStruct({
    systemPrompt = "",
    contextTexts = [],
    chatHistory = [],
    userPrompt = "",
    attachments = [], // This is the specific attachment for only this prompt
  }) {
    const VISION_MODELS = [
      "llama-3.2-90b-vision-preview",
      "llama-3.2-11b-vision-preview",
    ];
    const DEFAULT_PROMPT_STRUCT = [
      {
        role: "system",
        content: `${systemPrompt}${this.#appendContext(contextTexts)}`,
      },
      ...chatHistory,
      { role: "user", content: userPrompt },
    ];

    // If there are no attachments or model is not a vision model, return the default prompt structure
    // as there is nothing to attach or do and no model limitations to consider
    if (!attachments.length) return DEFAULT_PROMPT_STRUCT;
    if (!VISION_MODELS.includes(this.model)) {
      this.#log(
        `${this.model} is not an explicitly supported vision model! Will omit attachments.`
      );
      return DEFAULT_PROMPT_STRUCT;
    }

    return [
      // Why is the system prompt and history commented out?
      // Note for the future:
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // {
      //   role: "user",
      // },
      // {
      //   role: "assistant",
      //   content: "OK",
      // },
      {
        role: "user",
        content: this.#generateContent({ userPrompt, attachments }),
      },
    ];
  }

  
  constructPrompt({
    systemPrompt = "",
    contextTexts = [],
    chatHistory = [],
    userPrompt = "",
    attachments = [], // This is the specific attachment for only this prompt
  }) {
    return this.#conditionalPromptStruct({
      systemPrompt,
      contextTexts,
      chatHistory,
      userPrompt,
      attachments,
    });
  }

  async getChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `GroqAI:chatCompletion: ${this.model} is not valid for chat completion!`
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
      !result.output.hasOwnProperty("choices") ||
      result.output.choices.length === 0
    )
      return null;

    return {
      textResponse: result.output.choices[0].message.content,
      metrics: {
        prompt_tokens: result.output.usage.prompt_tokens || 0,
        completion_tokens: result.output.usage.completion_tokens || 0,
        total_tokens: result.output.usage.total_tokens || 0,
        outputTps:
          result.output.usage.completion_tokens /
          result.output.usage.completion_time,
        duration: result.output.usage.total_time,
        model: this.model,
        timestamp: new Date(),
      },
    };
  }

  async streamGetChatCompletion(messages = null, { temperature = 0.7 }) {
    if (!(await this.isValidChatCompletionModel(this.model)))
      throw new Error(
        `GroqAI:streamChatCompletion: ${this.model} is not valid for chat completion!`
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

  handleStream(response, stream, responseProps) {
    return handleDefaultStreamResponseV2(response, stream, responseProps);
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
  GroqLLM,
};
