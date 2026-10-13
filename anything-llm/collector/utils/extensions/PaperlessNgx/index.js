const fs = require("fs");
const path = require("path");
const { default: slugify } = require("slugify");
const { v4 } = require("uuid");
const {
  writeToServerDocuments,
  sanitizeFileName,
  documentsFolder,
} = require("../../files");
const { tokenizeString } = require("../../tokenizer");
const { validBaseUrl } = require("../../http");
const PaperlessNgxLoader = require("./PaperlessNgxLoader");


async function loadPaperlessNgx({ baseUrl = null, apiToken = null }, response) {
  if (!baseUrl || !validBaseUrl(baseUrl)) {
    return {
      success: false,
      reason: "Provided base URL is not a valid URL.",
    };
  }

  if (!apiToken) {
    return {
      success: false,
      reason:
        "You need to provide an API token to use the Paperless-ngx connector.",
    };
  }

  const { origin, hostname } = new URL(baseUrl);
  const loader = new PaperlessNgxLoader({
    baseUrl: origin,
    apiToken,
  });

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
      reason:
        error ?? "No parseable documents found in that Paperless-ngx instance.",
      data: null,
    };
  }

  const outFolder = slugify(
    `paperless-${hostname}-${v4().slice(0, 4)}`
  ).toLowerCase();
  const outFolderPath = path.resolve(documentsFolder, outFolder);
  if (!fs.existsSync(outFolderPath))
    fs.mkdirSync(outFolderPath, { recursive: true });

  docs.forEach((doc) => {
    if (!doc.pageContent) return;

    const data = {
      id: v4(),
      url: doc.metadata.url,
      title: doc.metadata.title,
      docAuthor: doc.metadata.correspondent || "Unknown",
      description: `A document from the Paperless-ngx instance at ${origin}`,
      docSource: `paperless-ngx`,
      chunkSource: generateChunkSource(
        { doc, baseUrl: origin, apiToken },
        response.locals.encryptionWorker
      ),
      published: doc.metadata.created,
      wordCount: doc.pageContent.split(" ").length,
      pageContent: doc.pageContent,
      token_count_estimate: tokenizeString(doc.pageContent),
    };
    const fileName = sanitizeFileName(
      `${slugify(doc.metadata.title)}-${data.id}`
    );
    writeToServerDocuments({
      data,
      filename: fileName,
      destinationOverride: outFolderPath,
    });
  });

  return {
    success: true,
    reason: null,
    data: {
      files: docs.length,
      destination: outFolder,
    },
  };
}


function generateChunkSource({ doc, baseUrl, apiToken }, encryptionWorker) {
  const payload = {
    baseUrl,
    token: apiToken,
  };
  return `paperless-ngx://${doc.metadata.id}?payload=${encryptionWorker.encrypt(
    JSON.stringify(payload)
  )}`;
}

module.exports = {
  loadPaperlessNgx,
};
