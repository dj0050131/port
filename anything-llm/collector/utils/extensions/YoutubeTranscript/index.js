const fs = require("fs");
const path = require("path");
const { default: slugify } = require("slugify");
const { v4 } = require("uuid");
const {
  writeToServerDocuments,
  sanitizeFileName,
  documentsFolder,
  isWithin,
} = require("../../files");
const { tokenizeString } = require("../../tokenizer");
const { YoutubeLoader } = require("./YoutubeLoader");
const { validYoutubeVideoUrl } = require("../../url");


async function fetchVideoTranscriptContent({ url }) {
  if (!validYoutubeVideoUrl(url)) {
    return {
      success: false,
      reason: "Invalid URL. Should be youtu.be or youtube.com/watch.",
      content: null,
      metadata: {},
    };
  }
  const loader = YoutubeLoader.createFromUrl(url, { addVideoInfo: true });
  const { docs, error } = await loader
    .load()
    .then((docs) => ({ docs, error: null }))
    .catch((e) => ({
      docs: [],
      error: e.message?.split("Error:")?.[1] || e.message,
    }));

  if (!docs.length || !!error) {
    return {
      success: false,
      reason: error ?? "No transcript found for that YouTube video.",
      content: null,
      metadata: {},
    };
  }

  const metadata = docs[0].metadata;
  const content = docs[0].pageContent;
  if (!content.length) {
    return {
      success: false,
      reason: "No transcript could be parsed for that YouTube video.",
      content: null,
      metadata: {},
    };
  }

  return {
    success: true,
    reason: null,
    content,
    metadata,
  };
}








async function loadYouTubeTranscript({ url }, options = { parseOnly: false }) {
  const transcriptResults = await fetchVideoTranscriptContent({ url });
  if (!transcriptResults.success) {
    return {
      success: false,
      reason:
        transcriptResults.reason ||
        "An unknown error occurred during transcription retrieval",
      documents: [],
      content: null,
      saveAsDocument: options.parseOnly,
      data: {},
    };
  }

  const { content, metadata } = transcriptResults;

  if (options.parseOnly) {
    return {
      success: true,
      reason: null,
      content: buildTranscriptContentWithMetadata(content, metadata),
      documents: [],
      saveAsDocument: options.parseOnly,
      data: {},
    };
  }

  const outFolder = sanitizeFileName(
    slugify(`${metadata.author} YouTube transcripts`).toLowerCase()
  );
  const outFolderPath = path.resolve(documentsFolder, outFolder);
  const uuid = v4();
  const fileName = sanitizeFileName(`${slugify(metadata.title)}-${uuid}`);

  if (!isWithin(documentsFolder, path.resolve(outFolderPath, fileName))) {
    console.error(
      `[YouTube Loader]: Invalid file path ${path.resolve(
        outFolderPath,
        fileName
      )} is not within the documents folder ${documentsFolder}`
    );
    return {
      success: false,
      reason: `[YouTube Loader]: Invalid file path ${path.resolve(
        outFolderPath,
        fileName
      )} is not within the documents folder ${documentsFolder}`,
      documents: [],
      data: {},
    };
  }

  if (!fs.existsSync(outFolderPath))
    fs.mkdirSync(outFolderPath, { recursive: true });
  const data = {
    id: uuid,
    url: url + ".youtube",
    title: metadata.title || url,
    docAuthor: metadata.author,
    description: metadata.description,
    docSource: url,
    chunkSource: `youtube://${url}`,
    published: new Date().toLocaleString(),
    wordCount: content.split(" ").length,
    pageContent: content,
    token_count_estimate: tokenizeString(content),
  };
  const document = writeToServerDocuments({
    data,
    filename: fileName,
    destinationOverride: outFolderPath,
  });

  return {
    success: true,
    reason: null,
    documents: [document],
    data: {
      title: metadata.title,
      author: metadata.author,
      destination: outFolder,
    },
  };
}


function buildTranscriptContentWithMetadata(content = "", metadata = {}) {
  const VALID_METADATA_KEYS = ["title", "author", "description", "view_count"];
  if (!content || !metadata || Object.keys(metadata).length === 0)
    return content;

  let contentWithMetadata = "";
  VALID_METADATA_KEYS.forEach((key) => {
    if (!metadata[key]) return;
    contentWithMetadata += `<${key}>${metadata[key]}</${key}>`;
  });
  return `${contentWithMetadata}\nTranscript:\n${content}`;
}

module.exports = {
  loadYouTubeTranscript,
  fetchVideoTranscriptContent,
};
