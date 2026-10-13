












function getVectorDbClass(getExactly = null) {
  const vectorSelection = getExactly ?? process.env.VECTOR_DB ?? "lancedb";
  switch (vectorSelection) {
    case "pinecone":
      const { Pinecone } = require("../vectorDbProviders/pinecone");
      return new Pinecone();
    case "chroma":
      const { Chroma } = require("../vectorDbProviders/chroma");
      return new Chroma();
    case "chromacloud":
      const { ChromaCloud } = require("../vectorDbProviders/chromacloud");
      return new ChromaCloud();
    case "lancedb":
      const { LanceDb } = require("../vectorDbProviders/lance");
      return new LanceDb();
    case "weaviate":
      const { Weaviate } = require("../vectorDbProviders/weaviate");
      return new Weaviate();
    case "qdrant":
      const { QDrant } = require("../vectorDbProviders/qdrant");
      return new QDrant();
    case "milvus":
      const { Milvus } = require("../vectorDbProviders/milvus");
      return new Milvus();
    case "zilliz":
      const { Zilliz } = require("../vectorDbProviders/zilliz");
      return new Zilliz();
    case "astra":
      const { AstraDB } = require("../vectorDbProviders/astra");
      return new AstraDB();
    case "pgvector":
      const { PGVector } = require("../vectorDbProviders/pgvector");
      return new PGVector();
    default:
      console.error(
        `\x1b[31m[ENV ERROR]\x1b[0m No VECTOR_DB value found in environment! Falling back to LanceDB`
      );
      const { LanceDb: DefaultLanceDb } = require("../vectorDbProviders/lance");
      return new DefaultLanceDb();
  }
}


function getLLMProvider({ provider = null, model = null } = {}) {
  const LLMSelection = provider ?? process.env.LLM_PROVIDER ?? "openai";
  const embedder = getEmbeddingEngineSelection();

  switch (LLMSelection) {
    case "openai":
      const { OpenAiLLM } = require("../AiProviders/openAi");
      return new OpenAiLLM(embedder, model);
    case "azure":
      const { AzureOpenAiLLM } = require("../AiProviders/azureOpenAi");
      return new AzureOpenAiLLM(embedder, model);
    case "anthropic":
      const { AnthropicLLM } = require("../AiProviders/anthropic");
      return new AnthropicLLM(embedder, model);
    case "gemini":
      const { GeminiLLM } = require("../AiProviders/gemini");
      return new GeminiLLM(embedder, model);
    case "lmstudio":
      const { LMStudioLLM } = require("../AiProviders/lmStudio");
      return new LMStudioLLM(embedder, model);
    case "localai":
      const { LocalAiLLM } = require("../AiProviders/localAi");
      return new LocalAiLLM(embedder, model);
    case "ollama":
      const { OllamaAILLM } = require("../AiProviders/ollama");
      return new OllamaAILLM(embedder, model);
    case "togetherai":
      const { TogetherAiLLM } = require("../AiProviders/togetherAi");
      return new TogetherAiLLM(embedder, model);
    case "fireworksai":
      const { FireworksAiLLM } = require("../AiProviders/fireworksAi");
      return new FireworksAiLLM(embedder, model);
    case "perplexity":
      const { PerplexityLLM } = require("../AiProviders/perplexity");
      return new PerplexityLLM(embedder, model);
    case "openrouter":
      const { OpenRouterLLM } = require("../AiProviders/openRouter");
      return new OpenRouterLLM(embedder, model);
    case "mistral":
      const { MistralLLM } = require("../AiProviders/mistral");
      return new MistralLLM(embedder, model);
    case "huggingface":
      const { HuggingFaceLLM } = require("../AiProviders/huggingface");
      return new HuggingFaceLLM(embedder, model);
    case "groq":
      const { GroqLLM } = require("../AiProviders/groq");
      return new GroqLLM(embedder, model);
    case "koboldcpp":
      const { KoboldCPPLLM } = require("../AiProviders/koboldCPP");
      return new KoboldCPPLLM(embedder, model);
    case "textgenwebui":
      const { TextGenWebUILLM } = require("../AiProviders/textGenWebUI");
      return new TextGenWebUILLM(embedder, model);
    case "cohere":
      const { CohereLLM } = require("../AiProviders/cohere");
      return new CohereLLM(embedder, model);
    case "litellm":
      const { LiteLLM } = require("../AiProviders/liteLLM");
      return new LiteLLM(embedder, model);
    case "generic-openai":
      const { GenericOpenAiLLM } = require("../AiProviders/genericOpenAi");
      return new GenericOpenAiLLM(embedder, model);
    case "bedrock":
      const { AWSBedrockLLM } = require("../AiProviders/bedrock");
      return new AWSBedrockLLM(embedder, model);
    case "deepseek":
      const { DeepSeekLLM } = require("../AiProviders/deepseek");
      return new DeepSeekLLM(embedder, model);
    case "apipie":
      const { ApiPieLLM } = require("../AiProviders/apipie");
      return new ApiPieLLM(embedder, model);
    case "novita":
      const { NovitaLLM } = require("../AiProviders/novita");
      return new NovitaLLM(embedder, model);
    case "xai":
      const { XAiLLM } = require("../AiProviders/xai");
      return new XAiLLM(embedder, model);
    case "nvidia-nim":
      const { NvidiaNimLLM } = require("../AiProviders/nvidiaNim");
      return new NvidiaNimLLM(embedder, model);
    case "ppio":
      const { PPIOLLM } = require("../AiProviders/ppio");
      return new PPIOLLM(embedder, model);
    case "moonshotai":
      const { MoonshotAiLLM } = require("../AiProviders/moonshotAi");
      return new MoonshotAiLLM(embedder, model);
    case "dpais":
      const { DellProAiStudioLLM } = require("../AiProviders/dellProAiStudio");
      return new DellProAiStudioLLM(embedder, model);
    case "cometapi":
      const { CometApiLLM } = require("../AiProviders/cometapi");
      return new CometApiLLM(embedder, model);
    case "foundry":
      const { FoundryLLM } = require("../AiProviders/foundry");
      return new FoundryLLM(embedder, model);
    case "zai":
      const { ZAiLLM } = require("../AiProviders/zai");
      return new ZAiLLM(embedder, model);
    case "giteeai":
      const { GiteeAILLM } = require("../AiProviders/giteeai");
      return new GiteeAILLM(embedder, model);
    case "docker-model-runner":
      const {
        DockerModelRunnerLLM,
      } = require("../AiProviders/dockerModelRunner");
      return new DockerModelRunnerLLM(embedder, model);
    default:
      throw new Error(
        `ENV: No valid LLM_PROVIDER value found in environment! Using ${process.env.LLM_PROVIDER}`
      );
  }
}

/**
 * Returns the EmbedderProvider by itself to whatever is currently in the system settings.
 * @returns {BaseEmbedderProvider}
 */
function getEmbeddingEngineSelection() {
  const { NativeEmbedder } = require("../EmbeddingEngines/native");
  const engineSelection = process.env.EMBEDDING_ENGINE;
  switch (engineSelection) {
    case "openai":
      const { OpenAiEmbedder } = require("../EmbeddingEngines/openAi");
      return new OpenAiEmbedder();
    case "azure":
      const {
        AzureOpenAiEmbedder,
      } = require("../EmbeddingEngines/azureOpenAi");
      return new AzureOpenAiEmbedder();
    case "localai":
      const { LocalAiEmbedder } = require("../EmbeddingEngines/localAi");
      return new LocalAiEmbedder();
    case "ollama":
      const { OllamaEmbedder } = require("../EmbeddingEngines/ollama");
      return new OllamaEmbedder();
    case "native":
      return new NativeEmbedder();
    case "lmstudio":
      const { LMStudioEmbedder } = require("../EmbeddingEngines/lmstudio");
      return new LMStudioEmbedder();
    case "cohere":
      const { CohereEmbedder } = require("../EmbeddingEngines/cohere");
      return new CohereEmbedder();
    case "voyageai":
      const { VoyageAiEmbedder } = require("../EmbeddingEngines/voyageAi");
      return new VoyageAiEmbedder();
    case "litellm":
      const { LiteLLMEmbedder } = require("../EmbeddingEngines/liteLLM");
      return new LiteLLMEmbedder();
    case "mistral":
      const { MistralEmbedder } = require("../EmbeddingEngines/mistral");
      return new MistralEmbedder();
    case "generic-openai":
      const {
        GenericOpenAiEmbedder,
      } = require("../EmbeddingEngines/genericOpenAi");
      return new GenericOpenAiEmbedder();
    case "gemini":
      const { GeminiEmbedder } = require("../EmbeddingEngines/gemini");
      return new GeminiEmbedder();
    case "openrouter":
      const { OpenRouterEmbedder } = require("../EmbeddingEngines/openRouter");
      return new OpenRouterEmbedder();
    default:
      return new NativeEmbedder();
  }
}


function getLLMProviderClass({ provider = null } = {}) {
  switch (provider) {
    case "openai":
      const { OpenAiLLM } = require("../AiProviders/openAi");
      return OpenAiLLM;
    case "azure":
      const { AzureOpenAiLLM } = require("../AiProviders/azureOpenAi");
      return AzureOpenAiLLM;
    case "anthropic":
      const { AnthropicLLM } = require("../AiProviders/anthropic");
      return AnthropicLLM;
    case "gemini":
      const { GeminiLLM } = require("../AiProviders/gemini");
      return GeminiLLM;
    case "lmstudio":
      const { LMStudioLLM } = require("../AiProviders/lmStudio");
      return LMStudioLLM;
    case "localai":
      const { LocalAiLLM } = require("../AiProviders/localAi");
      return LocalAiLLM;
    case "ollama":
      const { OllamaAILLM } = require("../AiProviders/ollama");
      return OllamaAILLM;
    case "togetherai":
      const { TogetherAiLLM } = require("../AiProviders/togetherAi");
      return TogetherAiLLM;
    case "fireworksai":
      const { FireworksAiLLM } = require("../AiProviders/fireworksAi");
      return FireworksAiLLM;
    case "perplexity":
      const { PerplexityLLM } = require("../AiProviders/perplexity");
      return PerplexityLLM;
    case "openrouter":
      const { OpenRouterLLM } = require("../AiProviders/openRouter");
      return OpenRouterLLM;
    case "mistral":
      const { MistralLLM } = require("../AiProviders/mistral");
      return MistralLLM;
    case "huggingface":
      const { HuggingFaceLLM } = require("../AiProviders/huggingface");
      return HuggingFaceLLM;
    case "groq":
      const { GroqLLM } = require("../AiProviders/groq");
      return GroqLLM;
    case "koboldcpp":
      const { KoboldCPPLLM } = require("../AiProviders/koboldCPP");
      return KoboldCPPLLM;
    case "textgenwebui":
      const { TextGenWebUILLM } = require("../AiProviders/textGenWebUI");
      return TextGenWebUILLM;
    case "cohere":
      const { CohereLLM } = require("../AiProviders/cohere");
      return CohereLLM;
    case "litellm":
      const { LiteLLM } = require("../AiProviders/liteLLM");
      return LiteLLM;
    case "generic-openai":
      const { GenericOpenAiLLM } = require("../AiProviders/genericOpenAi");
      return GenericOpenAiLLM;
    case "bedrock":
      const { AWSBedrockLLM } = require("../AiProviders/bedrock");
      return AWSBedrockLLM;
    case "deepseek":
      const { DeepSeekLLM } = require("../AiProviders/deepseek");
      return DeepSeekLLM;
    case "apipie":
      const { ApiPieLLM } = require("../AiProviders/apipie");
      return ApiPieLLM;
    case "novita":
      const { NovitaLLM } = require("../AiProviders/novita");
      return NovitaLLM;
    case "xai":
      const { XAiLLM } = require("../AiProviders/xai");
      return XAiLLM;
    case "nvidia-nim":
      const { NvidiaNimLLM } = require("../AiProviders/nvidiaNim");
      return NvidiaNimLLM;
    case "ppio":
      const { PPIOLLM } = require("../AiProviders/ppio");
      return PPIOLLM;
    case "dpais":
      const { DellProAiStudioLLM } = require("../AiProviders/dellProAiStudio");
      return DellProAiStudioLLM;
    case "moonshotai":
      const { MoonshotAiLLM } = require("../AiProviders/moonshotAi");
      return MoonshotAiLLM;
    case "cometapi":
      const { CometApiLLM } = require("../AiProviders/cometapi");
      return CometApiLLM;
    case "foundry":
      const { FoundryLLM } = require("../AiProviders/foundry");
      return FoundryLLM;
    case "zai":
      const { ZAiLLM } = require("../AiProviders/zai");
      return ZAiLLM;
    case "giteeai":
      const { GiteeAILLM } = require("../AiProviders/giteeai");
      return GiteeAILLM;
    case "docker-model-runner":
      const {
        DockerModelRunnerLLM,
      } = require("../AiProviders/dockerModelRunner");
      return DockerModelRunnerLLM;
    default:
      return null;
  }
}


function getBaseLLMProviderModel({ provider = null } = {}) {
  switch (provider) {
    case "openai":
      return process.env.OPEN_MODEL_PREF;
    case "azure":
      return process.env.OPEN_MODEL_PREF;
    case "anthropic":
      return process.env.ANTHROPIC_MODEL_PREF;
    case "gemini":
      return process.env.GEMINI_LLM_MODEL_PREF;
    case "lmstudio":
      return process.env.LMSTUDIO_MODEL_PREF;
    case "localai":
      return process.env.LOCAL_AI_MODEL_PREF;
    case "ollama":
      return process.env.OLLAMA_MODEL_PREF;
    case "togetherai":
      return process.env.TOGETHER_AI_MODEL_PREF;
    case "fireworksai":
      return process.env.FIREWORKS_AI_LLM_MODEL_PREF;
    case "perplexity":
      return process.env.PERPLEXITY_MODEL_PREF;
    case "openrouter":
      return process.env.OPENROUTER_MODEL_PREF;
    case "mistral":
      return process.env.MISTRAL_MODEL_PREF;
    case "huggingface":
      return null;
    case "groq":
      return process.env.GROQ_MODEL_PREF;
    case "koboldcpp":
      return process.env.KOBOLD_CPP_MODEL_PREF;
    case "textgenwebui":
      return null;
    case "cohere":
      return process.env.COHERE_MODEL_PREF;
    case "litellm":
      return process.env.LITE_LLM_MODEL_PREF;
    case "generic-openai":
      return process.env.GENERIC_OPEN_AI_MODEL_PREF;
    case "bedrock":
      return process.env.AWS_BEDROCK_LLM_MODEL_PREFERENCE;
    case "deepseek":
      return process.env.DEEPSEEK_MODEL_PREF;
    case "apipie":
      return process.env.APIPIE_LLM_MODEL_PREF;
    case "novita":
      return process.env.NOVITA_LLM_MODEL_PREF;
    case "xai":
      return process.env.XAI_LLM_MODEL_PREF;
    case "nvidia-nim":
      return process.env.NVIDIA_NIM_LLM_MODEL_PREF;
    case "ppio":
      return process.env.PPIO_MODEL_PREF;
    case "dpais":
      return process.env.DPAIS_LLM_MODEL_PREF;
    case "moonshotai":
      return process.env.MOONSHOT_AI_MODEL_PREF;
    case "cometapi":
      return process.env.COMETAPI_LLM_MODEL_PREF;
    case "foundry":
      return process.env.FOUNDRY_MODEL_PREF;
    case "zai":
      return process.env.ZAI_MODEL_PREF;
    case "giteeai":
      return process.env.GITEE_AI_MODEL_PREF;
    case "docker-model-runner":
      return process.env.DOCKER_MODEL_RUNNER_LLM_MODEL_PREF;
    default:
      return null;
  }
}

// Some models have lower restrictions on chars that can be encoded in a single pass
// and by default we assume it can handle 1,000 chars, but some models use work with smaller
function maximumChunkLength() {
  if (
    !!process.env.EMBEDDING_MODEL_MAX_CHUNK_LENGTH &&
    !isNaN(process.env.EMBEDDING_MODEL_MAX_CHUNK_LENGTH) &&
    Number(process.env.EMBEDDING_MODEL_MAX_CHUNK_LENGTH) > 1
  )
    return Number(process.env.EMBEDDING_MODEL_MAX_CHUNK_LENGTH);

  return 1_000;
}

function toChunks(arr, size) {
  return Array.from({ length: Math.ceil(arr.length / size) }, (_v, i) =>
    arr.slice(i * size, i * size + size)
  );
}

function humanFileSize(bytes, si = false, dp = 1) {
  const thresh = si ? 1000 : 1024;

  if (Math.abs(bytes) < thresh) {
    return bytes + " B";
  }

  const units = si
    ? ["kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
    : ["KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"];
  let u = -1;
  const r = 10 ** dp;

  do {
    bytes /= thresh;
    ++u;
  } while (
    Math.round(Math.abs(bytes) * r) / r >= thresh &&
    u < units.length - 1
  );

  return bytes.toFixed(dp) + " " + units[u];
}

module.exports = {
  getEmbeddingEngineSelection,
  maximumChunkLength,
  getVectorDbClass,
  getLLMProviderClass,
  getBaseLLMProviderModel,
  getLLMProvider,
  toChunks,
  humanFileSize,
};
