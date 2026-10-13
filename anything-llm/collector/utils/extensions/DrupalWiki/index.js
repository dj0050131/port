

const { DrupalWiki } = require("./DrupalWiki");
const { validBaseUrl } = require("../../../utils/http");

async function loadAndStoreSpaces(
  { baseUrl = null, spaceIds = null, accessToken = null },
  response
) {
  if (!baseUrl) {
    return {
      success: false,
      reason:
        "Please provide your baseUrl like https://mywiki.drupal-wiki.net.",
    };
  } else if (!validBaseUrl(baseUrl)) {
    return {
      success: false,
      reason: "Provided base URL is not a valid URL.",
    };
  }

  if (!spaceIds) {
    return {
      success: false,
      reason:
        "Please provide a list of spaceIds like 21,56,67 you want to extract",
    };
  }

  if (!accessToken) {
    return {
      success: false,
      reason: "Please provide a REST API-Token.",
    };
  }
  const drupalWiki = new DrupalWiki({ baseUrl, accessToken });

  const encryptionWorker = response.locals.encryptionWorker;
  const spaceIdsArr = spaceIds.split(",").map((idStr) => {
    return Number(idStr.trim());
  });

  for (const spaceId of spaceIdsArr) {
    try {
      await drupalWiki.loadAndStoreAllPagesForSpace(spaceId, encryptionWorker);
    } catch (e) {
      console.error(e);
      return {
        success: false,
        reason: e.message,
        data: {},
      };
    }
  }

  return {
    success: true,
    reason: null,
    data: {
      spaceIds,
      destination: drupalWiki.storagePath,
    },
  };
}

/**
 * Gets the page content from a specific Confluence page, not all pages in a workspace.
 * @returns
 */
async function loadPage({ baseUrl, pageId, accessToken }) {
  const drupalWiki = new DrupalWiki({ baseUrl, accessToken });
  try {
    const page = await drupalWiki.loadPage(pageId);
    return {
      success: true,
      reason: null,
      content: page.processedBody,
    };
  } catch (e) {
    return {
      success: false,
      reason: `Failed (re)-fetching DrupalWiki page ${pageId} form ${baseUrl}}`,
      content: null,
    };
  }
}

module.exports = {
  loadAndStoreSpaces,
  loadPage,
};
