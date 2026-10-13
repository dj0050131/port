const { v4: uuidv4 } = require("uuid");
const { DocumentManager } = require("../DocumentManager");
const { WorkspaceChats } = require("../../models/workspaceChats");
const { getVectorDbClass, getLLMProvider } = require("../helpers");
const { writeResponseChunk } = require("../helpers/chat/responses");
const {
  chatPrompt,
  sourceIdentifier,
  recentChatHistory,
  grepAllSlashCommands,
} = require("./index");
const {
  EphemeralAgentHandler,
  EphemeralEventListener,
} = require("../agents/ephemeral");
const { Telemetry } = require("../../models/telemetry");
const { CollectorApi } = require("../collectorApi");
const fs = require("fs");
const path = require("path");
const { hotdirPath, normalizePath, isWithin } = require("../files");



async function processDocumentAttachments(attachments = []) {
  if (!Array.isArray(attachments) || attachments.length === 0)
    return { parsedDocuments: [], imageAttachments: [] };
  const documentAttachments = [];
  const imageAttachments = [];
  for (const attachment of attachments) {
    if (
      attachment &&
      attachment.contentString &&
      attachment.mime &&
      attachment.mime.toLowerCase() === "application/anythingllm-document"
    )
      documentAttachments.push(attachment);
    else imageAttachments.push(attachment);
  }

  if (documentAttachments.length === 0)
    return { parsedDocuments: [], imageAttachments };
  const Collector = new CollectorApi();
  const processingOnline = await Collector.online();
  if (!processingOnline) {
    console.warn(
      "Collector API is not online, skipping document attachment processing"
    );
    return { parsedDocuments: [], imageAttachments };
  }
  if (!fs.existsSync(hotdirPath)) fs.mkdirSync(hotdirPath, { recursive: true });

  const parsedDocuments = [];
  for (const attachment of documentAttachments) {
    try {
      let base64Data = attachment.contentString;
      const dataUriMatch = base64Data.match(/^data:[^;]+;base64,(.+)$/);
      if (dataUriMatch) base64Data = dataUriMatch[1];

      const buffer = Buffer.from(base64Data, "base64");
      const filename = normalizePath(
        attachment.name || `attachment-${uuidv4()}`
      );
      const filePath = normalizePath(path.join(hotdirPath, filename));
      if (!isWithin(hotdirPath, filePath))
        throw new Error(`Invalid file path for attachment ${filename}`);
      fs.writeFileSync(filePath, buffer);

      const { success, reason, documents } =
        await Collector.parseDocument(filename);
      if (success && documents?.length > 0) parsedDocuments.push(...documents);
      else console.warn(`Failed to parse attachment ${filename}:`, reason);
    } catch (error) {
      console.error(
        `Error processing attachment ${attachment.name}:`,
        error.message
      );
    }
  }

  return { parsedDocuments, imageAttachments };
}


async function chatSync({
  workspace,
  message = null,
  mode = "chat",
  user = null,
  thread = null,
  sessionId = null,
  attachments = [],
  reset = false,
}) {
  const uuid = uuidv4();
  const chatMode = mode ?? "chat";

  if (reset) {
    await WorkspaceChats.markThreadHistoryInvalidV2({
      workspaceId: workspace.id,
      user_id: user?.id,
      thread_id: thread?.id,
      api_session_id: sessionId,
    });
    if (!message?.length) {
      return {
        id: uuid,
        type: "textResponse",
        textResponse: "Chat history was reset!",
        sources: [],
        close: true,
        error: null,
        metrics: {},
      };
    }
  }

  // Process slash commands
  // Since preset commands are not supported in API calls, we can just process the message here
  const processedMessage = await grepAllSlashCommands(message);
  message = processedMessage;

  if (EphemeralAgentHandler.isAgentInvocation({ message })) {
    await Telemetry.sendTelemetry("agent_chat_started");

    const agentHandler = new EphemeralAgentHandler({
      uuid,
      workspace,
      prompt: message,
      userId: user?.id || null,
      threadId: thread?.id || null,
      sessionId,
    });

    const eventListener = new EphemeralEventListener();
    await agentHandler.init();
    await agentHandler.createAIbitat({ handler: eventListener });
    agentHandler.startAgentCluster();

    // The cluster has started and now we wait for close event since
    return await eventListener
      .waitForClose()
      .then(async ({ thoughts, textResponse }) => {
        await WorkspaceChats.new({
          workspaceId: workspace.id,
          prompt: String(message),
          response: {
            text: textResponse,
            sources: [],
            attachments,
            type: chatMode,
            thoughts,
          },
          include: false,
          apiSessionId: sessionId,
        });
        return {
          id: uuid,
          type: "textResponse",
          sources: [],
          close: true,
          error: null,
          textResponse,
          thoughts,
        };
      });
  }

  const LLMConnector = getLLMProvider({
    provider: workspace?.chatProvider,
    model: workspace?.chatModel,
  });
  const VectorDb = getVectorDbClass();
  const messageLimit = workspace?.openAiHistory || 20;
  const hasVectorizedSpace = await VectorDb.hasNamespace(workspace.slug);
  const embeddingsCount = await VectorDb.namespaceCount(workspace.slug);

  // User is trying to query-mode chat a workspace that has no data in it - so
  if ((!hasVectorizedSpace || embeddingsCount === 0) && chatMode === "query") {
    const textResponse =
      workspace?.queryRefusalResponse ??
      "There is no relevant information in this workspace to answer your query.";

    await WorkspaceChats.new({
      workspaceId: workspace.id,
      prompt: String(message),
      response: {
        text: textResponse,
        sources: [],
        attachments: attachments,
        type: chatMode,
        metrics: {},
      },
      include: false,
      apiSessionId: sessionId,
    });

    return {
      id: uuid,
      type: "textResponse",
      sources: [],
      close: true,
      error: null,
      textResponse,
      metrics: {},
    };
  }

  let contextTexts = [];
  let sources = [];
  let pinnedDocIdentifiers = [];
  const { rawHistory, chatHistory } = await recentChatHistory({
    user,
    workspace,
    thread,
    messageLimit,
    apiSessionId: sessionId,
  });

  await new DocumentManager({
    workspace,
    maxTokens: LLMConnector.promptWindowLimit(),
  })
    .pinnedDocs()
    .then((pinnedDocs) => {
      pinnedDocs.forEach((doc) => {
        const { pageContent, ...metadata } = doc;
        pinnedDocIdentifiers.push(sourceIdentifier(doc));
        contextTexts.push(doc.pageContent);
        sources.push({
          text:
            pageContent.slice(0, 1_000) +
            "...continued on in source document...",
          ...metadata,
        });
      });
    });

  const processedAttachments = await processDocumentAttachments(attachments);
  const parsedAttachments = processedAttachments.parsedDocuments;
  attachments = processedAttachments.imageAttachments;
  parsedAttachments.forEach((doc) => {
    if (doc.pageContent) {
      contextTexts.push(doc.pageContent);
      const { pageContent, ...metadata } = doc;
      sources.push({
        text:
          pageContent.slice(0, 1_000) + "...continued on in source document...",
        ...metadata,
      });
    }
  });

  const vectorSearchResults =
    embeddingsCount !== 0
      ? await VectorDb.performSimilaritySearch({
          namespace: workspace.slug,
          input: message,
          LLMConnector,
          similarityThreshold: workspace?.similarityThreshold,
          topN: workspace?.topN,
          filterIdentifiers: pinnedDocIdentifiers,
          rerank: workspace?.vectorSearchMode === "rerank",
        })
      : {
          contextTexts: [],
          sources: [],
          message: null,
        };

  if (!!vectorSearchResults.message) {
    return {
      id: uuid,
      type: "abort",
      textResponse: null,
      sources: [],
      close: true,
      error: vectorSearchResults.message,
      metrics: {},
    };
  }

  const { fillSourceWindow } = require("../helpers/chat");
  const filledSources = fillSourceWindow({
    nDocs: workspace?.topN || 4,
    searchResults: vectorSearchResults.sources,
    history: rawHistory,
    filterIdentifiers: pinnedDocIdentifiers,
  });

  // Why does contextTexts get all the info, but sources only get current search?
  // This is to give the ability of the LLM to "comprehend" a contextual response without
  // populating the Citations under a response with documents the user "thinks" are irrelevant
  // If a past citation was used to answer the question - that is visible in the history so it logically makes sense
  contextTexts = [...contextTexts, ...filledSources.contextTexts];
  sources = [...sources, ...vectorSearchResults.sources];

  // If in query mode and no context chunks are found from search, backfill, or pins -  do not
  // let the LLM try to hallucinate a response or use general knowledge and exit early
  if (chatMode === "query" && contextTexts.length === 0) {
    const textResponse =
      workspace?.queryRefusalResponse ??
      "There is no relevant information in this workspace to answer your query.";

    await WorkspaceChats.new({
      workspaceId: workspace.id,
      prompt: message,
      response: {
        text: textResponse,
        sources: [],
        attachments: attachments,
        type: chatMode,
        metrics: {},
      },
      threadId: thread?.id || null,
      include: false,
      apiSessionId: sessionId,
      user,
    });

    return {
      id: uuid,
      type: "textResponse",
      sources: [],
      close: true,
      error: null,
      textResponse,
      metrics: {},
    };
  }

  // Compress & Assemble message to ensure prompt passes token limit with room for response
  const messages = await LLMConnector.compressMessages(
    {
      systemPrompt: await chatPrompt(workspace, user),
      userPrompt: message,
      contextTexts,
      chatHistory,
      attachments,
    },
    rawHistory
  );

  const { textResponse, metrics: performanceMetrics } =
    await LLMConnector.getChatCompletion(messages, {
      temperature: workspace?.openAiTemp ?? LLMConnector.defaultTemp,
      user: user,
    });

  if (!textResponse) {
    return {
      id: uuid,
      type: "abort",
      textResponse: null,
      sources: [],
      close: true,
      error: "No text completion could be completed with this input.",
      metrics: performanceMetrics,
    };
  }

  const { chat } = await WorkspaceChats.new({
    workspaceId: workspace.id,
    prompt: message,
    response: {
      text: textResponse,
      sources,
      attachments,
      type: chatMode,
      metrics: performanceMetrics,
    },
    threadId: thread?.id || null,
    apiSessionId: sessionId,
    user,
  });

  return {
    id: uuid,
    type: "textResponse",
    close: true,
    error: null,
    chatId: chat.id,
    textResponse,
    sources,
    metrics: performanceMetrics,
  };
}


async function streamChat({
  response,
  workspace,
  message = null,
  mode = "chat",
  user = null,
  thread = null,
  sessionId = null,
  attachments = [],
  reset = false,
}) {
  const uuid = uuidv4();
  const chatMode = mode ?? "chat";

  if (reset) {
    await WorkspaceChats.markThreadHistoryInvalidV2({
      workspaceId: workspace.id,
      user_id: user?.id,
      thread_id: thread?.id,
      api_session_id: sessionId,
    });
    if (!message?.length) {
      writeResponseChunk(response, {
        id: uuid,
        type: "textResponse",
        textResponse: "Chat history was reset!",
        sources: [],
        attachments: [],
        close: true,
        error: null,
        metrics: {},
      });
      return;
    }
  }

  // Check for and process slash commands
  // Since preset commands are not supported in API calls, we can just process the message here
  const processedMessage = await grepAllSlashCommands(message);
  message = processedMessage;

  if (EphemeralAgentHandler.isAgentInvocation({ message })) {
    await Telemetry.sendTelemetry("agent_chat_started");

    const agentHandler = new EphemeralAgentHandler({
      uuid,
      workspace,
      prompt: message,
      userId: user?.id || null,
      threadId: thread?.id || null,
      sessionId,
    });

    const eventListener = new EphemeralEventListener();
    await agentHandler.init();
    await agentHandler.createAIbitat({ handler: eventListener });
    agentHandler.startAgentCluster();

    // The cluster has started and now we wait for close event since
    return eventListener
      .streamAgentEvents(response, uuid)
      .then(async ({ thoughts, textResponse }) => {
        await WorkspaceChats.new({
          workspaceId: workspace.id,
          prompt: String(message),
          response: {
            text: textResponse,
            sources: [],
            attachments: attachments,
            type: chatMode,
            thoughts,
          },
          include: true,
          threadId: thread?.id || null,
          apiSessionId: sessionId,
        });
        writeResponseChunk(response, {
          uuid,
          type: "finalizeResponseStream",
          textResponse,
          thoughts,
          close: true,
          error: false,
        });
      });
  }

  const LLMConnector = getLLMProvider({
    provider: workspace?.chatProvider,
    model: workspace?.chatModel,
  });

  const VectorDb = getVectorDbClass();
  const messageLimit = workspace?.openAiHistory || 20;
  const hasVectorizedSpace = await VectorDb.hasNamespace(workspace.slug);
  const embeddingsCount = await VectorDb.namespaceCount(workspace.slug);

  // User is trying to query-mode chat a workspace that has no data in it - so
  if ((!hasVectorizedSpace || embeddingsCount === 0) && chatMode === "query") {
    const textResponse =
      workspace?.queryRefusalResponse ??
      "There is no relevant information in this workspace to answer your query.";
    writeResponseChunk(response, {
      id: uuid,
      type: "textResponse",
      textResponse,
      sources: [],
      attachments: [],
      close: true,
      error: null,
      metrics: {},
    });
    await WorkspaceChats.new({
      workspaceId: workspace.id,
      prompt: message,
      response: {
        text: textResponse,
        sources: [],
        attachments: attachments,
        type: chatMode,
        metrics: {},
      },
      threadId: thread?.id || null,
      apiSessionId: sessionId,
      include: false,
      user,
    });
    return;
  }

  let completeText;
  let metrics = {};
  let contextTexts = [];
  let sources = [];
  let pinnedDocIdentifiers = [];
  const { rawHistory, chatHistory } = await recentChatHistory({
    user,
    workspace,
    thread,
    messageLimit,
    apiSessionId: sessionId,
  });

  // However we limit the maximum of appended context to 80% of its overall size, mostly because if it expands beyond this
  await new DocumentManager({
    workspace,
    maxTokens: LLMConnector.promptWindowLimit(),
  })
    .pinnedDocs()
    .then((pinnedDocs) => {
      pinnedDocs.forEach((doc) => {
        const { pageContent, ...metadata } = doc;
        pinnedDocIdentifiers.push(sourceIdentifier(doc));
        contextTexts.push(doc.pageContent);
        sources.push({
          text:
            pageContent.slice(0, 1_000) +
            "...continued on in source document...",
          ...metadata,
        });
      });
    });

  const processedAttachments = await processDocumentAttachments(attachments);
  const parsedAttachments = processedAttachments.parsedDocuments;
  attachments = processedAttachments.imageAttachments;
  parsedAttachments.forEach((doc) => {
    if (doc.pageContent) {
      contextTexts.push(doc.pageContent);
      const { pageContent, ...metadata } = doc;
      sources.push({
        text:
          pageContent.slice(0, 1_000) + "...continued on in source document...",
        ...metadata,
      });
    }
  });

  const vectorSearchResults =
    embeddingsCount !== 0
      ? await VectorDb.performSimilaritySearch({
          namespace: workspace.slug,
          input: message,
          LLMConnector,
          similarityThreshold: workspace?.similarityThreshold,
          topN: workspace?.topN,
          filterIdentifiers: pinnedDocIdentifiers,
          rerank: workspace?.vectorSearchMode === "rerank",
        })
      : {
          contextTexts: [],
          sources: [],
          message: null,
        };

  if (!!vectorSearchResults.message) {
    writeResponseChunk(response, {
      id: uuid,
      type: "abort",
      textResponse: null,
      sources: [],
      close: true,
      error: vectorSearchResults.message,
      metrics: {},
    });
    return;
  }

  const { fillSourceWindow } = require("../helpers/chat");
  const filledSources = fillSourceWindow({
    nDocs: workspace?.topN || 4,
    searchResults: vectorSearchResults.sources,
    history: rawHistory,
    filterIdentifiers: pinnedDocIdentifiers,
  });

  // Why does contextTexts get all the info, but sources only get current search?
  // This is to give the ability of the LLM to "comprehend" a contextual response without
  // populating the Citations under a response with documents the user "thinks" are irrelevant
  // If a past citation was used to answer the question - that is visible in the history so it logically makes sense
  contextTexts = [...contextTexts, ...filledSources.contextTexts];
  sources = [...sources, ...vectorSearchResults.sources];

  // If in query mode and no context chunks are found from search, backfill, or pins -  do not
  // let the LLM try to hallucinate a response or use general knowledge and exit early
  if (chatMode === "query" && contextTexts.length === 0) {
    const textResponse =
      workspace?.queryRefusalResponse ??
      "There is no relevant information in this workspace to answer your query.";
    writeResponseChunk(response, {
      id: uuid,
      type: "textResponse",
      textResponse,
      sources: [],
      close: true,
      error: null,
      metrics: {},
    });

    await WorkspaceChats.new({
      workspaceId: workspace.id,
      prompt: message,
      response: {
        text: textResponse,
        sources: [],
        attachments: attachments,
        type: chatMode,
        metrics: {},
      },
      threadId: thread?.id || null,
      apiSessionId: sessionId,
      include: false,
      user,
    });
    return;
  }

  // Compress & Assemble message to ensure prompt passes token limit with room for response
  const messages = await LLMConnector.compressMessages(
    {
      systemPrompt: await chatPrompt(workspace, user),
      userPrompt: message,
      contextTexts,
      chatHistory,
      attachments,
    },
    rawHistory
  );

  // If streaming is not explicitly enabled for connector
  if (LLMConnector.streamingEnabled() !== true) {
    const { textResponse, metrics: performanceMetrics } =
      await LLMConnector.getChatCompletion(messages, {
        temperature: workspace?.openAiTemp ?? LLMConnector.defaultTemp,
        user: user,
      });
    completeText = textResponse;
    metrics = performanceMetrics;
    writeResponseChunk(response, {
      uuid,
      sources,
      type: "textResponseChunk",
      textResponse: completeText,
      close: true,
      error: false,
      metrics,
    });
  } else {
    const stream = await LLMConnector.streamGetChatCompletion(messages, {
      temperature: workspace?.openAiTemp ?? LLMConnector.defaultTemp,
      user: user,
    });
    completeText = await LLMConnector.handleStream(response, stream, { uuid });
    metrics = stream.metrics;
  }

  if (completeText?.length > 0) {
    const { chat } = await WorkspaceChats.new({
      workspaceId: workspace.id,
      prompt: message,
      response: {
        text: completeText,
        sources,
        type: chatMode,
        metrics,
        attachments,
      },
      threadId: thread?.id || null,
      apiSessionId: sessionId,
      user,
    });

    writeResponseChunk(response, {
      uuid,
      type: "finalizeResponseStream",
      close: true,
      error: false,
      chatId: chat.id,
      metrics,
      sources,
    });
    return;
  }

  writeResponseChunk(response, {
    uuid,
    type: "finalizeResponseStream",
    close: true,
    error: false,
  });
  return;
}

module.exports.ApiChatHandler = {
  chatSync,
  streamChat,
};
