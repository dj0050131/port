const { TokenManager } = require("../tiktoken");







class LLMPerformanceMonitor {
  static tokenManager = new TokenManager();
  
  static countTokens(messages = []) {
    try {
      return this.tokenManager.statsFrom(messages);
    } catch (e) {
      return 0;
    }
  }
  
  static measureAsyncFunction(func) {
    return (async () => {
      const start = Date.now();
      const output = await func; // is a promise
      const end = Date.now();
      const duration = output?.usage?.duration ?? (end - start) / 1000;
      return { output, duration };
    })();
  }

  
  static async measureStream({
    func,
    messages = [],
    runPromptTokenCalculation = true,
    modelTag = "",
  }) {
    const stream = await func;
    stream.start = Date.now();
    stream.duration = 0;
    stream.metrics = {
      completion_tokens: 0,
      prompt_tokens: runPromptTokenCalculation ? this.countTokens(messages) : 0,
      total_tokens: 0,
      outputTps: 0,
      duration: 0,
      ...(modelTag ? { model: modelTag } : {}),
    };

    stream.endMeasurement = (reportedUsage = {}) => {
      const end = Date.now();
      const estimatedDuration = (end - stream.start) / 1000;

      // Merge the reported usage with the existing metrics
      // so the math in the metrics object is correct when calculating
      stream.metrics = {
        ...stream.metrics,
        ...reportedUsage,
        duration: reportedUsage?.duration ?? estimatedDuration,
        timestamp: new Date(),
      };

      stream.metrics.total_tokens =
        stream.metrics.prompt_tokens + (stream.metrics.completion_tokens || 0);
      stream.metrics.outputTps =
        stream.metrics.completion_tokens / stream.metrics.duration;
      return stream.metrics;
    };
    return stream;
  }
}

module.exports = {
  LLMPerformanceMonitor,
};
