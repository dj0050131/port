const { loadSummarizationChain } = require("langchain/chains");
const { PromptTemplate } = require("@langchain/core/prompts");
const { RecursiveCharacterTextSplitter } = require("@langchain/textsplitters");
const Provider = require("../providers/ai-provider");



async function summarizeContent({
  provider = "openai",
  model = null,
  controllerSignal,
  content,
}) {
  const llm = Provider.LangChainChatModel(provider, {
    temperature: 0,
    model: model,
  });

  const textSplitter = new RecursiveCharacterTextSplitter({
    separators: ["\n\n", "\n"],
    chunkSize: 10000,
    chunkOverlap: 500,
  });
  const docs = await textSplitter.createDocuments([content]);

  const mapPrompt = `
      Write a detailed summary of the following text for a research purpose:
      "{text}"
      SUMMARY:
      `;

  const mapPromptTemplate = new PromptTemplate({
    template: mapPrompt,
    inputVariables: ["text"],
  });

  const chain = loadSummarizationChain(llm, {
    type: "map_reduce",
    combinePrompt: mapPromptTemplate,
    combineMapPrompt: mapPromptTemplate,
    verbose: process.env.NODE_ENV === "development",
  });

  const res = await chain.call({
    ...(controllerSignal ? { signal: controllerSignal } : {}),
    input_documents: docs,
  });

  return res.text;
}

module.exports = { summarizeContent };
