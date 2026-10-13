const { v4: uuidv4 } = require("uuid");
const moment = require("moment");

function clientAbortedHandler(resolve, fullText) {
  resolve(fullText);
  return;
}


function handleDefaultStreamResponseV2(response, stream, responseProps) {
  const { uuid = uuidv4(), sources = [] } = responseProps;

  // Why are we doing this?
  let hasUsageMetrics = false;
  let usage = {
    // prompt_tokens can be in this object if the provider supports it - otherwise we manually count it
    completion_tokens: 0,
  };

  return new Promise(async (resolve) => {
    let fullText = "";

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

        // If we see usage metrics in the chunk, we can use them directly
        // instead of estimating them, but we only want to assign values if
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

function convertToChatHistory(history = []) {
  const formattedHistory = [];
  for (const record of history) {
    const { prompt, response, createdAt, feedbackScore = null, id } = record;
    const data = JSON.parse(response);

    if (typeof prompt !== "string") {
      continue;
    } else if (typeof data.text !== "string") {
      continue;
    }

    formattedHistory.push([
      {
        role: "user",
        content: prompt,
        sentAt: moment(createdAt).unix(),
        attachments: data?.attachments ?? [],
        chatId: id,
      },
      {
        type: data?.type || "chart",
        role: "assistant",
        content: data.text,
        sources: data.sources || [],
        chatId: id,
        sentAt: moment(createdAt).unix(),
        feedbackScore,
        metrics: data?.metrics || {},
      },
    ]);
  }

  return formattedHistory.flat();
}


function convertToPromptHistory(history = []) {
  const formattedHistory = [];
  for (const record of history) {
    const { prompt, response } = record;
    const data = JSON.parse(response);

    if (typeof prompt !== "string") {
      continue;
    } else if (typeof data.text !== "string") {
      continue;
    }

    formattedHistory.push([
      {
        role: "user",
        content: prompt,
        ...(data?.attachments?.length > 0
          ? { attachments: data?.attachments }
          : {}),
      },
      {
        role: "assistant",
        content: data.text,
      },
    ]);
  }
  return formattedHistory.flat();
}

/**
 * Safely stringifies any object containing BigInt values
 * @param {*} obj - Anything to stringify that might contain BigInt values
 * @returns {string} JSON string with BigInt values converted to strings
 */
function safeJSONStringify(obj) {
  return JSON.stringify(obj, (_, value) => {
    if (typeof value === "bigint") return value.toString();
    return value;
  });
}

function writeResponseChunk(response, data) {
  response.write(`data: ${safeJSONStringify(data)}\n\n`);
  return;
}


function formatChatHistory(
  chatHistory = [],
  formatterFunction,
  mode = "asProperty"
) {
  return chatHistory.map((historicalMessage) => {
    if (
      historicalMessage?.role !== "user" || // Only user messages can have attachments
      !historicalMessage?.attachments || // If there are no attachments, we can skip this
      !historicalMessage.attachments.length // If there is an array but it is empty, we can skip this
    )
      return historicalMessage;

    if (mode === "spread") {
      return {
        role: historicalMessage.role,
        ...formatterFunction({
          userPrompt: historicalMessage.content,
          attachments: historicalMessage.attachments,
        }),
      };
    }

    return {
      role: historicalMessage.role,
      content: formatterFunction({
        userPrompt: historicalMessage.content,
        attachments: historicalMessage.attachments,
      }),
    };
  });
}

module.exports = {
  handleDefaultStreamResponseV2,
  convertToChatHistory,
  convertToPromptHistory,
  writeResponseChunk,
  clientAbortedHandler,
  formatChatHistory,
  safeJSONStringify,
};
