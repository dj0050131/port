const path = require("path");
const fs = require("fs");
const LEGACY_MODEL_MAP = require("./legacy");

class ContextWindowFinder {
  static instance = null;
  static modelMap = LEGACY_MODEL_MAP;

  
  static trackedProviders = {
    anthropic: "anthropic",
    openai: "openai",
    cohere: "cohere_chat",
    gemini: "vertex_ai-language-models",
    groq: "groq",
    xai: "xai",
    deepseek: "deepseek",
    moonshot: "moonshot",
    zai: "vercel_ai_gateway", // Vercel has correct context windows for Z.AI models
  };
  static expiryMs = 1000 * 60 * 60 * 24 * 3; // 3 days
  static remoteUrl =
    "https://raw.githubusercontent.com/BerriAI/litellm/main/model_prices_and_context_window.json";

  cacheLocation = path.resolve(
    process.env.STORAGE_DIR
      ? path.resolve(process.env.STORAGE_DIR, "models", "context-windows")
      : path.resolve(__dirname, `../../../storage/models/context-windows`)
  );
  cacheFilePath = path.resolve(this.cacheLocation, "context-windows.json");
  cacheFileExpiryPath = path.resolve(this.cacheLocation, ".cached_at");
  seenStaleCacheWarning = false;

  constructor() {
    if (ContextWindowFinder.instance) return ContextWindowFinder.instance;
    ContextWindowFinder.instance = this;
    if (!fs.existsSync(this.cacheLocation))
      fs.mkdirSync(this.cacheLocation, { recursive: true });

    // If the cache is stale or not found at all, pull the model map from remote
    if (this.isCacheStale || !fs.existsSync(this.cacheFilePath)) {
      this.#pullRemoteModelMap().catch((err) =>
        this.log("Background model map pull failed:", err)
      );
    }
  }

  log(text, ...args) {
  }

  /**
   * Checks if the cache is stale by checking if the cache file exists and if the cache file is older than the expiry time.
   * @returns {boolean}
   */
  get isCacheStale() {
    if (!fs.existsSync(this.cacheFileExpiryPath)) return true;
    const cachedAt = fs.readFileSync(this.cacheFileExpiryPath, "utf8");
    return Date.now() - cachedAt > ContextWindowFinder.expiryMs;
  }

  
  get cachedModelMap() {
    if (!fs.existsSync(this.cacheFilePath)) {
      this.log(`\x1b[33m
--------------------------------
[WARNING] Model map cache is not found!
Invalid context windows will be returned leading to inaccurate model responses
or smaller context windows than expected.
You can fix this by restarting AnythingLLM so the model map is re-pulled.
--------------------------------\x1b[0m`);
      return null;
    }

    if (this.isCacheStale && !this.seenStaleCacheWarning) {
      this.log(
        "Model map cache is stale - some model context windows may be incorrect. This is OK and the model map will be re-pulled on next boot."
      );
      this.seenStaleCacheWarning = true;
    }

    return JSON.parse(
      fs.readFileSync(this.cacheFilePath, { encoding: "utf8" })
    );
  }

  
  async #pullRemoteModelMap() {
    try {
      this.log("Pulling remote model map...");
      const response = await fetch(ContextWindowFinder.remoteUrl);
      if (response.status !== 200) {
        throw new Error(
          "Failed to fetch remote model map - non 200 status code"
        );
      }

      const data = await response.json();
      const modelMap = this.#validateModelMap(this.#formatModelMap(data));
      await Promise.all([
        fs.promises.writeFile(
          this.cacheFilePath,
          JSON.stringify(modelMap, null, 2)
        ),
        fs.promises.writeFile(this.cacheFileExpiryPath, Date.now().toString()),
      ]);

      this.log("Remote model map synced and cached");
      return modelMap;
    } catch (error) {
      this.log("Error syncing remote model map", error);
      return null;
    }
  }

  #validateModelMap(modelMap = {}) {
    for (const [provider, models] of Object.entries(modelMap)) {
      // If the models is null/falsey or has no keys, throw an error
      if (typeof models !== "object")
        throw new Error(
          `Invalid model map for ${provider} - models is not an object`
        );
      if (!models || Object.keys(models).length === 0)
        throw new Error(`Invalid model map for ${provider} - no models found!`);

      for (const [model, contextWindow] of Object.entries(models)) {
        if (isNaN(contextWindow) || contextWindow <= 0) {
          this.log(
            `${provider}:${model} - context window is not a positive number. Got ${contextWindow}.`
          );
          delete models[model];
          continue;
        }
      }
    }
    return modelMap;
  }

  
  #formatModelMap(modelMap = {}) {
    const formattedModelMap = {};

    for (const [provider, liteLLMProviderTag] of Object.entries(
      ContextWindowFinder.trackedProviders
    )) {
      formattedModelMap[provider] = {};
      const matches = Object.entries(modelMap).filter(
        ([_key, config]) => config.litellm_provider === liteLLMProviderTag
      );
      for (const [key, config] of matches) {
        const contextWindow = Number(config.max_input_tokens);
        if (isNaN(contextWindow)) continue;

        // Some models have a provider/model-tag format, so we need to get the last part since we dont do paths
        const modelName = key.split("/").pop();
        formattedModelMap[provider][modelName] = contextWindow;
      }
    }
    return formattedModelMap;
  }

  
  get(provider = null, model = null) {
    if (!provider || !this.cachedModelMap || !this.cachedModelMap[provider])
      return null;
    if (!model) return this.cachedModelMap[provider];

    const modelContextWindow = this.cachedModelMap[provider][model];
    if (!modelContextWindow) {
      this.log("Invalid access to model context window - not found in cache", {
        provider,
        model,
      });
      return null;
    }
    return Number(modelContextWindow);
  }
}

module.exports = { MODEL_MAP: new ContextWindowFinder() };
