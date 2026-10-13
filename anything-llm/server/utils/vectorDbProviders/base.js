/**
 * Base class for all Vector Database providers.
 * All vector database providers should extend this class and implement/override the necessary methods.
 */
class VectorDatabase {
  get name() {
    return "VectorDatabase";
  }

  constructor() {
    if (this.constructor === VectorDatabase) {
      throw new Error("VectorDatabase cannot be instantiated directly");
    }
  }

  
  async connect() {
    throw new Error("Must be implemented by provider");
  }

  
  async heartbeat() {
    throw new Error("Must be implemented by provider");
  }

  
  async totalVectors() {
    throw new Error("Must be implemented by provider");
  }

  
  async namespaceCount(namespace = null) {
    throw new Error("Must be implemented by provider");
  }

  
  async namespace(client, namespace = null) {
    throw new Error("Must be implemented by provider");
  }

  
  async hasNamespace(namespace = null) {
    throw new Error("Must be implemented by provider");
  }

  
  async namespaceExists(client, namespace = null) {
    throw new Error("Must be implemented by provider");
  }

  
  async deleteVectorsInNamespace(client, namespace = null) {
    throw new Error("Must be implemented by provider");
  }

  
  async addDocumentToNamespace(
    namespace,
    documentData = {},
    fullFilePath = null,
    skipCache = false
  ) {
    throw new Error("Must be implemented by provider");
  }

  
  async deleteDocumentFromNamespace(namespace, docId) {
    throw new Error("Must be implemented by provider");
  }

  
  async performSimilaritySearch({
    namespace = null,
    input = "",
    LLMConnector = null,
    similarityThreshold = 0.25,
    topN = 4,
    filterIdentifiers = [],
  }) {
    throw new Error("Must be implemented by provider");
  }

  
  async similarityResponse({
    client,
    namespace,
    queryVector,
    similarityThreshold = 0.25,
    topN = 4,
    filterIdentifiers = [],
  }) {
    throw new Error("Must be implemented by provider");
  }

  
  async "namespace-stats"(reqBody = {}) {
    throw new Error("Must be implemented by provider");
  }

  
  async "delete-namespace"(reqBody = {}) {
    throw new Error("Must be implemented by provider");
  }

  
  async reset() {
    throw new Error("Must be implemented by provider");
  }

  /**
   * Curate sources from search results
   * @param {any[]} sources - Sources to curate
   * @returns {any[]}
   */
  curateSources(sources = []) {
    throw new Error("Must be implemented by provider");
  }

  logger(message = null, ...args) {
  }
}

module.exports = { VectorDatabase };
