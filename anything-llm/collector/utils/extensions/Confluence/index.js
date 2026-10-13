const fs = require("fs");
const path = require("path");
const { default: slugify } = require("slugify");
const { v4 } = require("uuid");
const { writeToServerDocuments, sanitizeFileName } = require("../../files");
const { tokenizeString } = require("../../tokenizer");
const { ConfluencePagesLoader } = require("./ConfluenceLoader");


async function loadConfluence(
  {
    baseUrl = null,
    spaceKey = null,
    username = null,
    accessToken = null,
    cloud = true,
    personalAccessToken = null,
    bypassSSL = false,
  },
  response
) {
  if (!personalAccessToken && (!username || !accessToken)) {
    return {
      success: false,
      reason:
        "You need either a personal access token (PAT), or a username and access token to use the Confluence connector.",
    };
  }

  if (!baseUrl || !validBaseUrl(baseUrl)) {
    return {
      success: false,
      reason: "Provided base URL is not a valid URL.",
    };
  }

  if (!spaceKey) {
    return {
      success: false,
      reason: "You need to provide a Confluence space key.",
    };
  }

  const { origin, hostname } = new URL(baseUrl);
  const loader = new ConfluencePagesLoader({
    baseUrl: origin, // Use the origin to avoid issues with subdomains, ports, protocols, etc.
    spaceKey,
    username,
    accessToken,
    cloud,
    personalAccessToken,
    bypassSSL,
  });

  const { docs, error } = await loader
    .load()
    .then((docs) => {
      return { docs, error: null };
    })
    .catch((e) => {
      return {
        docs: [],
        error: e.message?.split("Error:")?.[1] || e.message,
      };
    });

  if (!docs.length || !!error) {
    return {
      success: false,
      reason: error ?? "No pages found for that Confluence space.",
    };
  }
  const outFolder = slugify(
    `confluence-${hostname}-${v4().slice(0, 4)}`
  ).toLowerCase();

  const outFolderPath =
    process.env.NODE_ENV === "development"
      ? path.resolve(
          __dirname,
          `../../../../server/storage/documents/${outFolder}`
        )
      : path.resolve(process.env.STORAGE_DIR, `documents/${outFolder}`);

  if (!fs.existsSync(outFolderPath))
    fs.mkdirSync(outFolderPath, { recursive: true });

  docs.forEach((doc) => {
    if (!doc.pageContent) return;

    const data = {
      id: v4(),
      url: doc.metadata.url + ".page",
      title: doc.metadata.title || doc.metadata.source,
      docAuthor: origin,
      description: doc.metadata.title,
      docSource: `${origin} Confluence`,
      chunkSource: generateChunkSource(
        {
          doc,
          baseUrl: origin,
          spaceKey,
          accessToken,
          username,
          cloud,
          bypassSSL,
        },
        response.locals.encryptionWorker
      ),
      published: new Date().toLocaleString(),
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
      spaceKey,
      destination: outFolder,
    },
  };
}

/**
 * Gets the page content from a specific Confluence page, not all pages in a workspace.
 * @returns
 */
async function fetchConfluencePage({
  pageUrl,
  baseUrl,
  spaceKey,
  username,
  accessToken,
  cloud = true,
  bypassSSL = false,
}) {
  if (!pageUrl || !baseUrl || !spaceKey || !username || !accessToken) {
    return {
      success: false,
      content: null,
      reason:
        "You need either a username and access token, or a personal access token (PAT), to use the Confluence connector.",
    };
  }

  if (!validBaseUrl(baseUrl)) {
    return {
      success: false,
      content: null,
      reason: "Provided base URL is not a valid URL.",
    };
  }

  if (!spaceKey) {
    return {
      success: false,
      content: null,
      reason: "You need to provide a Confluence space key.",
    };
  }
  const loader = new ConfluencePagesLoader({
    baseUrl, // Should be the origin of the baseUrl
    spaceKey,
    username,
    accessToken,
    cloud,
    bypassSSL,
  });

  const { docs, error } = await loader
    .load()
    .then((docs) => {
      return { docs, error: null };
    })
    .catch((e) => {
      return {
        docs: [],
        error: e.message?.split("Error:")?.[1] || e.message,
      };
    });

  if (!docs.length || !!error) {
    return {
      success: false,
      reason: error ?? "No pages found for that Confluence space.",
      content: null,
    };
  }

  const targetDocument = docs.find(
    (doc) => doc.pageContent && doc.metadata.url === pageUrl
  );
  if (!targetDocument) {
    return {
      success: false,
      reason: "Target page could not be found in Confluence space.",
      content: null,
    };
  }

  return {
    success: true,
    reason: null,
    content: targetDocument.pageContent,
  };
}

/**
 * Validates if the provided baseUrl is a valid URL at all.
 * @param {string} baseUrl
 * @returns {boolean}
 */
function validBaseUrl(baseUrl) {
  try {
    new URL(baseUrl);
    return true;
  } catch (e) {
    return false;
  }
}


function generateChunkSource(
  { doc, baseUrl, spaceKey, accessToken, username, cloud, bypassSSL },
  encryptionWorker
) {
  const payload = {
    baseUrl,
    spaceKey,
    token: accessToken,
    username,
    cloud,
    bypassSSL,
  };
  return `confluence://${doc.metadata.url}?payload=${encryptionWorker.encrypt(
    JSON.stringify(payload)
  )}`;
}

module.exports = {
  loadConfluence,
  fetchConfluencePage,
};
