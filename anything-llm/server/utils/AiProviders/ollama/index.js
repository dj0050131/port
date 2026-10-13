const {
  writeResponseChunk,
  clientAbortedHandler,
  formatChatHistory,
} = require("../../helpers/chat/responses");
const { NativeEmbedder } = require("../../EmbeddingEngines/native");
const {
  LLMPerformanceMonitor,
} = require("../../helpers/chat/LLMPerformanceMonitor");
const { Ollama } = require("ollama");
const { v4: uuidv4 } = require("uuid");

class OllamaAILLM {
  /** @see OllamaAILLM.cacheContextWindows */
  static modelContextWindows = {};

  constructor(embedder = null, modelPreference = null) {
    if (!process.env.OLLAMA_BASE_PATH)
      throw new Error("No Ollama Base Path was set.");

    this.className = "OllamaAILLM";
    this.authToken = process.env.OLLAMA_AUTH_TOKEN;
    this.basePath = process.env.OLLAMA_BASE_PATH;
    this.model = modelPreference || process.env.OLLAMA_MODEL_PREF;
    this.keepAlive = process.env.OLLAMA_KEEP_ALIVE_TIMEOUT
      ? Number(process.env.OLLAMA_KEEP_ALIVE_TIMEOUT)
      : 300; // Default 5-minute timeout for Ollama model loading.

    const headers = this.authToken
      ? { Authorization: `Bearer ${this.authToken}` }
      : {};
    this.client = new Ollama({
      host: this.basePath,
      headers: headers,
      fetch: this.#applyFetch(),
    });
    this.embedder = embedder ?? new NativeEmbedder();
    this.defaultTemp = 0.7;

    // Lazy load the limits to avoid blocking the main thread on cacheContextWindows
    this.limits = null;

    OllamaAILLM.cacheContextWindows(true);
    this.#log(`initialized with model: ${this.model}`);
  }

  #log(text, ...args) {
  }

  static #slog(text, ...args) {
  }

  async assertModelContextLimits() {
    if (this.limits !== null) return;
    await OllamaAILLM.cacheContextWindows();
    this.limits = {
      history: this.promptWindowLimit() * 0.15,
      system: this.promptWindowLimit() * 0.15,
      user: this.promptWindowLimit() * 0.7,
    };
    this.#log(
      `model ${this.model} is using a max context window of ${this.promptWindowLimit()}/${OllamaAILLM.maxContextWindow(this.model)} tokens.`
    );
  }

  
  static async cacheContextWindows(force = false) {
    try {
      // Skip if we already have cached context windows and we're not forcing a refresh
      if (Object.keys(OllamaAILLM.modelContextWindows).length > 0 && !force)
        return;

      const authToken = process.env.OLLAMA_AUTH_TOKEN;
      const basePath = process.env.OLLAMA_BASE_PATH;
      const client = new Ollama({
        host: basePath,
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      });

      const { models } = await client.list().catch(() => ({ models: [] }));
      if (!models.length) return;

      const infoPromises = models.map((model) =>
        client
          .show({ model: model.name })
          .then((info) => ({ name: model.name, ...info }))
      );
      const infos = await Promise.all(infoPromises);
      infos.forEach((showInfo) => {
        if (showInfo.capabilities.includes("embedding")) return;
        const contextWindowKey = Object.keys(showInfo.model_info).find((key) =>
          key.endsWith(".context_length")
        );
        if (!contextWindowKey)
          return (OllamaAILLM.modelContextWindows[showInfo.name] = 4096);
        OllamaAILLM.modelContextWindows[showInfo.name] =
          showInfo.model_info[contextWindowKey];
      });
      OllamaAILLM.#slog(`Context windows cached for all models!`);
    } catch (e) {
      OllamaAILLM.#slog(`Error caching context windows`, e);
      return;
    }
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

  /**
   * Apply a custom fetch function to the Ollama client.
   * This is useful when we want to bypass the default 5m timeout for global fetch
   * for machines which run responses very slowly.
   * @returns {Function} The custom fetch function.
   */
  #applyFetch() {
    try {
      if (!("OLLAMA_RESPONSE_TIMEOUT" in process.env)) return fetch;
      const { Agent } = require("undici");
      const moment = require("moment");
      let timeout = process.env.OLLAMA_RESPONSE_TIMEOUT;

      if (!timeout || isNaN(Number(timeout)) || Number(timeout) <= 5 * 60_000) {
        this.#log(
          "Timeout option was not set, is not a number, or is less than 5 minutes in ms - falling back to default",
          { timeout }
        );
        return fetch;
      } else timeout = Number(timeout);

      const noTimeoutFetch = (input, init = {}) => {
        return fetch(input, {
          ...init,
          dispatcher: new Agent({ headersTimeout: timeout }),
        });
      };

      const humanDiff = moment.duration(timeout).humanize();
      this.#log(`Applying custom fetch w/timeout of ${humanDiff}.`);
      return noTimeoutFetch;
    } catch (error) {
      this.#log("Error applying custom fetch - using default fetch", error);
      return fetch;
    }
  }

  streamingEnabled() {
    return "streamGetChatCompletion" in this;
  }

  static promptWindowLimit(modelName) {
    if (Object.keys(OllamaAILLM.modelContextWindows).length === 0) {
      this.#slog(
        "No context windows cached - Context window may be inaccurately reported."
      );
      return process.env.OLLAMA_MODEL_TOKEN_LIMIT || 4096;
    }

    let userDefinedLimit = null;
    const systemDefinedLimit = OllamaAILLM.maxContextWindow(modelName);

    if (
      process.env.OLLAMA_MODEL_TOKEN_LIMIT &&
      !isNaN(Number(process.env.OLLAMA_MODEL_TOKEN_LIMIT)) &&
      Number(process.env.OLLAMA_MODEL_TOKEN_LIMIT) > 0
    )
      userDefinedLimit = Number(process.env.OLLAMA_MODEL_TOKEN_LIMIT);

    // The user defined limit is always higher priority than the context window limit, but it cannot be higher than the context window limit
    if (userDefinedLimit !== null)
      return Math.min(userDefinedLimit, systemDefinedLimit);

    // This prevents super-large context windows from being used if the user does not specify a value
    return Math.min(systemDefinedLimit, 16384);
  }

  promptWindowLimit() {
    return this.constructor.promptWindowLimit(this.model);
  }

  static maxContextWindow(modelName = null) {
    if (Object.keys(OllamaAILLM.modelContextWindows).length === 0 || !modelName)
      return 4096;
    return Number(OllamaAILLM.modelContextWindows[modelName]) || 16384;
  }

  async isValidChatCompletionModel(_ = "") {
    return true;
  }

  
  #generateContent({ userPrompt, attachments = [] }) {
    if (!attachments.length) return { content: userPrompt };
    const images = attachments.map(
      (attachment) => attachment.contentString.split("base64,").slice(-1)[0]
    );
    return { content: userPrompt, images };
  }

  /**
   * Handles errors from the Ollama API to make them more user friendly.
   * @param {Error} e
   */
  #errorHandler(e) {
    switch (e.message) {
      case "fetch failed":
        throw new Error(
          "Your Ollama instance could not be reached or is not responding. Please make sure it is running the API server and your connection information is correct in AnythingLLM."
        );
      default:
        return e;
    }
  }

  
  constructPrompt({
    systemPrompt = "",
    contextTexts = [],
    chatHistory = [],
    userPrompt = "",
    attachments = [],
  }) {
    const prompt = {
      role: "system",
      content: `${systemPrompt}${this.#appendContext(contextTexts)}`,
    };
    return [
      prompt,
      ...formatChatHistory(chatHistory, this.#generateContent, "spread"),
      {
        role: "user",
        ...this.#generateContent({ userPrompt, attachments }),
      },
    ];
  }

  async getChatCompletion(messages = null, { temperature = 0.7 }) {
    const result = await LLMPerformanceMonitor.measureAsyncFunction(
      this.client
        .chat({
          model: this.model,
          stream: false,
          messages,
          keep_alive: this.keepAlive,
          options: {
            temperature,
            use_mlock: true,
            num_ctx: this.promptWindowLimit(),
          },
        })
        .then((res) => {
          let content = res.message.content;
          if (res.message.thinking)
            content = `<think>${res.message.thinking}</think>${content}`;
          return {
            content,
            usage: {
              prompt_tokens: res.prompt_eval_count,
              completion_tokens: res.eval_count,
              total_tokens: res.prompt_eval_count + res.eval_count,
              duration: res.eval_duration / 1e9,
            },
          };
        })
        .catch((e) => {
          throw new Error(
            `Ollama::getChatCompletion failed to communicate with Ollama. ${this.#errorHandler(e).message}`
          );
        })
    );

    if (!result.output.content || !result.output.content.length)
      throw new Error(`Ollama::getChatCompletion text response was empty.`);

    return {
      textResponse: result.output.content,
      metrics: {
        prompt_tokens: result.output.usage.prompt_tokens,
        completion_tokens: result.output.usage.completion_tokens,
        total_tokens: result.output.usage.total_tokens,
        outputTps:
          result.output.usage.completion_tokens / result.output.usage.duration,
        duration: result.output.usage.duration,
        model: this.model,
        timestamp: new Date(),
      },
    };
  }

  async streamGetChatCompletion(messages = null, { temperature = 0.7 }) {
    const measuredStreamRequest = await LLMPerformanceMonitor.measureStream({
      func: this.client.chat({
        model: this.model,
        stream: true,
        messages,
        keep_alive: this.keepAlive,
        options: {
          temperature,
          use_mlock: true,
          num_ctx: this.promptWindowLimit(),
        },
      }),
      messages,
      runPromptTokenCalculation: false,
      modelTag: this.model,
    }).catch((e) => {
      throw this.#errorHandler(e);
    });
    return measuredStreamRequest;
  }

  
  handleStream(response, stream, responseProps) {
    const { uuid = uuidv4(), sources = [] } = responseProps;

    return new Promise(async (resolve) => {
      let fullText = "";
      let reasoningText = "";
      let usage = {
        prompt_tokens: 0,
        completion_tokens: 0,
      };

      // Establish listener to early-abort a streaming response
      const handleAbort = () => {
        stream?.endMeasurement(usage);
        clientAbortedHandler(resolve, fullText);
      };
      response.on("close", handleAbort);

      try {
        for await (const chunk of stream) {
          if (chunk === undefined)
            throw new Error(
              "Stream returned undefined chunk. Aborting reply - check model provider logs."
            );

          if (chunk.done) {
            usage.prompt_tokens = chunk.prompt_eval_count;
            usage.completion_tokens = chunk.eval_count;
            usage.duration = chunk.eval_duration / 1e9;
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

          if (chunk.hasOwnProperty("message")) {
            const content = chunk.message.content;
            const reasoningToken = chunk.message.thinking;

            if (reasoningToken) {
              if (reasoningText.length === 0) {
                const startTag = "<think>";
                writeResponseChunk(response, {
                  uuid,
                  sources,
                  type: "textResponseChunk",
                  textResponse: startTag + reasoningToken,
                  close: false,
                  error: false,
                });
                reasoningText += startTag + reasoningToken;
              } else {
                writeResponseChunk(response, {
                  uuid,
                  sources,
                  type: "textResponseChunk",
                  textResponse: reasoningToken,
                  close: false,
                  error: false,
                });
                reasoningText += reasoningToken;
              }
            } else if (content.length > 0) {
              if (reasoningText.length > 0) {
                const endTag = "</think>";
                writeResponseChunk(response, {
                  uuid,
                  sources,
                  type: "textResponseChunk",
                  textResponse: endTag,
                  close: false,
                  error: false,
                });
                fullText += reasoningText + endTag;
                reasoningText = ""; // Reset reasoning buffer
              }
              fullText += content; // Append regular text
              writeResponseChunk(response, {
                uuid,
                sources,
                type: "textResponseChunk",
                textResponse: content,
                close: false,
                error: false,
              });
            }
          }
        }
      } catch (error) {
        writeResponseChunk(response, {
          uuid,
          sources: [],
          type: "textResponseChunk",
          textResponse: "",
          close: true,
          error: `Ollama:streaming - could not stream chat. ${
            error?.cause ?? error.message
          }`,
        });
        response.removeListener("close", handleAbort);
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
    await this.assertModelContextLimits();
    const { messageArrayCompressor } = require("../../helpers/chat");
    const messageArray = this.constructPrompt(promptArgs);
    return await messageArrayCompressor(this, messageArray, rawHistory);
  }
}

module.exports = {
  OllamaAILLM,
};
