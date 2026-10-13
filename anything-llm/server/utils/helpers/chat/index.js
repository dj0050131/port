const { sourceIdentifier } = require("../../chats");
const { safeJsonParse } = require("../../http");
const { TokenManager } = require("../tiktoken");
const { convertToPromptHistory } = require("./responses");



async function messageArrayCompressor(llm, messages = [], rawHistory = []) {
  const tokenBuffer = 600;
  const tokenManager = new TokenManager(llm.model);
  if (tokenManager.statsFrom(messages) + tokenBuffer < llm.promptWindowLimit())
    return messages;

  const system = messages.shift();
  const user = messages.pop();
  const userPromptSize = tokenManager.countFromString(user.content);

  // User prompt is the main focus here - we we prioritize it and allow
  if (userPromptSize > llm.limits.user) {
    return [
      {
        role: "user",
        content: cannonball({
          input: user.content,
          targetTokenSize: llm.promptWindowLimit() * 0.8,
          tiktokenInstance: tokenManager,
        }),
      },
    ];
  }

  const compressedSystem = new Promise(async (resolve) => {
    const count = tokenManager.countFromString(system.content);
    if (count < llm.limits.system) {
      resolve(system);
      return;
    }

    const [prompt, context = ""] = system.content.split("Context:");
    let compressedPrompt;
    let compressedContext;

    // If the user system prompt contribution's to the system prompt is more than
    // 25% of the system limit, we will cannonball it - this favors the context
    if (tokenManager.countFromString(prompt) >= llm.limits.system * 0.25) {
      compressedPrompt = cannonball({
        input: prompt,
        targetTokenSize: llm.limits.system * 0.25,
        tiktokenInstance: tokenManager,
      });
    } else {
      compressedPrompt = prompt;
    }

    if (tokenManager.countFromString(context) >= llm.limits.system * 0.75) {
      compressedContext = cannonball({
        input: context,
        targetTokenSize: llm.limits.system * 0.75,
        tiktokenInstance: tokenManager,
      });
    } else {
      compressedContext = context;
    }

    system.content = `${compressedPrompt}${
      compressedContext ? `\nContext: ${compressedContext}` : ""
    }`;
    resolve(system);
  });

  const compressedPrompt = new Promise(async (resolve) => resolve(user));

  // We always aggressively compress history because it is the least
  // important data to retain in full-fidelity.
  const compressedHistory = new Promise((resolve) => {
    const eligibleHistoryItems = [];
    var historyTokenCount = 0;

    for (const [i, history] of rawHistory.reverse().entries()) {
      const [user, assistant] = convertToPromptHistory([history]);
      const [userTokens, assistantTokens] = [
        tokenManager.countFromString(user.content),
        tokenManager.countFromString(assistant.content),
      ];
      const total = userTokens + assistantTokens;

      if (historyTokenCount + total < llm.limits.history) {
        eligibleHistoryItems.unshift(user, assistant);
        historyTokenCount += total;
        continue;
      }

      // If we reach here the overhead of adding this history item will
      // the most recent 3 message pairs - if we are already past those - exit loop and stop
      if (i > 2) break;

      const maxTargetSize = Math.floor(llm.limits.history / 2.2);
      if (userTokens > maxTargetSize) {
        user.content = cannonball({
          input: user.content,
          targetTokenSize: maxTargetSize,
          tiktokenInstance: tokenManager,
        });
      }

      if (assistantTokens > maxTargetSize) {
        assistant.content = cannonball({
          input: assistant.content,
          targetTokenSize: maxTargetSize,
          tiktokenInstance: tokenManager,
        });
      }

      const newTotal = tokenManager.statsFrom([user, assistant]);
      if (historyTokenCount + newTotal > llm.limits.history) continue;
      eligibleHistoryItems.unshift(user, assistant);
      historyTokenCount += newTotal;
    }
    resolve(eligibleHistoryItems);
  });

  const [cSystem, cHistory, cPrompt] = await Promise.all([
    compressedSystem,
    compressedHistory,
    compressedPrompt,
  ]);
  return [cSystem, ...cHistory, cPrompt];
}

async function messageStringCompressor(llm, promptArgs = {}, rawHistory = []) {
  const tokenBuffer = 600;
  const tokenManager = new TokenManager(llm.model);
  const initialPrompt = llm.constructPrompt(promptArgs);
  if (
    tokenManager.statsFrom(initialPrompt) + tokenBuffer <
    llm.promptWindowLimit()
  )
    return initialPrompt;

  const system = promptArgs.systemPrompt;
  const user = promptArgs.userPrompt;
  const userPromptSize = tokenManager.countFromString(user);

  // User prompt is the main focus here - we we prioritize it and allow
  if (userPromptSize > llm.limits.user) {
    return llm.constructPrompt({
      userPrompt: cannonball({
        input: user,
        targetTokenSize: llm.promptWindowLimit() * 0.8,
        tiktokenInstance: tokenManager,
      }),
    });
  }

  const compressedSystem = new Promise(async (resolve) => {
    const count = tokenManager.countFromString(system);
    if (count < llm.limits.system) {
      resolve(system);
      return;
    }
    resolve(
      cannonball({
        input: system,
        targetTokenSize: llm.limits.system,
        tiktokenInstance: tokenManager,
      })
    );
  });

  const compressedPrompt = new Promise(async (resolve) => resolve(user));

  // We always aggressively compress history because it is the least
  // important data to retain in full-fidelity.
  const compressedHistory = new Promise((resolve) => {
    const eligibleHistoryItems = [];
    var historyTokenCount = 0;

    for (const [i, history] of rawHistory.reverse().entries()) {
      const [user, assistant] = convertToPromptHistory([history]);
      const [userTokens, assistantTokens] = [
        tokenManager.countFromString(user.content),
        tokenManager.countFromString(assistant.content),
      ];
      const total = userTokens + assistantTokens;

      if (historyTokenCount + total < llm.limits.history) {
        eligibleHistoryItems.unshift(user, assistant);
        historyTokenCount += total;
        continue;
      }

      // If we reach here the overhead of adding this history item will
      // the most recent 3 message pairs - if we are already past those - exit loop and stop
      if (i > 2) break;

      const maxTargetSize = Math.floor(llm.limits.history / 2.2);
      if (userTokens > maxTargetSize) {
        user.content = cannonball({
          input: user.content,
          targetTokenSize: maxTargetSize,
          tiktokenInstance: tokenManager,
        });
      }

      if (assistantTokens > maxTargetSize) {
        assistant.content = cannonball({
          input: assistant.content,
          targetTokenSize: maxTargetSize,
          tiktokenInstance: tokenManager,
        });
      }

      const newTotal = tokenManager.statsFrom([user, assistant]);
      if (historyTokenCount + newTotal > llm.limits.history) continue;
      eligibleHistoryItems.unshift(user, assistant);
      historyTokenCount += newTotal;
    }
    resolve(eligibleHistoryItems);
  });

  const [cSystem, cHistory, cPrompt] = await Promise.all([
    compressedSystem,
    compressedHistory,
    compressedPrompt,
  ]);

  return llm.constructPrompt({
    systemPrompt: cSystem,
    contextTexts: promptArgs?.contextTexts || [],
    chatHistory: cHistory,
    userPrompt: cPrompt,
  });
}

// Cannonball prompting: aka where we shoot a proportionally big cannonball through a proportional large prompt
function cannonball({
  input = "",
  targetTokenSize = 0,
  tiktokenInstance = null,
  ellipsesStr = null,
}) {
  if (!input || !targetTokenSize) return input;
  const tokenManager = tiktokenInstance || new TokenManager();
  const truncText = ellipsesStr || "\n\n--prompt truncated for brevity--\n\n";
  const initialInputSize = tokenManager.countFromString(input);
  if (initialInputSize < targetTokenSize) return input;

  const delta = initialInputSize - targetTokenSize;
  const tokenChunks = tokenManager.tokensFromString(input);
  const middleIdx = Math.floor(tokenChunks.length / 2);

  const leftChunks = tokenChunks.slice(0, middleIdx - Math.round(delta / 2));
  const rightChunks = tokenChunks.slice(middleIdx + Math.round(delta / 2));
  const truncatedText =
    tokenManager.bytesFromTokens(leftChunks) +
    truncText +
    tokenManager.bytesFromTokens(rightChunks);} tokens.`
  );
  return truncatedText;
}


function fillSourceWindow({
  nDocs = 4, // Number of documents
  searchResults = [], // Sources from similarity search
  history = [], // Raw history
  filterIdentifiers = [], // pinned document sources
} = config) {
  const sources = [...searchResults];

  if (sources.length >= nDocs || history.length === 0) {
    return {
      sources,
      contextTexts: sources.map((src) => src.text),
    };
  }

  const log = (text, ...args) => {
  };

  log(
    `Need to backfill ${nDocs - searchResults.length} chunks to fill in the source window for RAG!`
  );
  const seenChunks = new Set(searchResults.map((source) => source.id));

  // Looking at this function by itself you may think that this loop could be extreme for long history chats,
  for (const chat of history.reverse()) {
    if (sources.length >= nDocs) {
      log(
        `Citations backfilled to ${nDocs} references from ${searchResults.length} original citations.`
      );
      break;
    }

    const chatSources =
      safeJsonParse(chat.response, { sources: [] })?.sources || [];
    if (!chatSources?.length || !Array.isArray(chatSources)) continue;

    const validSources = chatSources.filter((source) => {
      return (
        filterIdentifiers.includes(sourceIdentifier(source)) == false && // source cannot be in current pins
        source.hasOwnProperty("score") && // source cannot have come from a pinned document that was previously pinned
        source.hasOwnProperty("text") && // source has a valid text property we can use
        seenChunks.has(source.id) == false // is unique
      );
    });

    for (const validSource of validSources) {
      if (sources.length >= nDocs) break;
      sources.push(validSource);
      seenChunks.add(validSource.id);
    }
  }

  return {
    sources,
    contextTexts: sources.map((src) => src.text),
  };
}

module.exports = {
  messageArrayCompressor,
  messageStringCompressor,
  fillSourceWindow,
};
