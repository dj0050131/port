const { CommunityHub } = require("../../models/communityHub");
const { reqBody } = require("../http");


function communityHubDownloadsEnabled(request, response, next) {
  if (!("COMMUNITY_HUB_BUNDLE_DOWNLOADS_ENABLED" in process.env)) {
    return response.status(422).json({
      error:
        "Community Hub bundle downloads are not enabled. The system administrator must enable this feature manually to allow this instance to download these types of items. See https://docs.anythingllm.com/configuration#anythingllm-hub-agent-skills",
    });
  }

  const item = response.locals.bundleItem;
  if (
    !item.verified &&
    item.visibility !== "private" &&
    process.env.COMMUNITY_HUB_BUNDLE_DOWNLOADS_ENABLED !== "allow_all"
  ) {
    return response.status(422).json({
      error:
        "Community hub bundle downloads are limited to verified public items or private team items only. Please contact the system administrator to review or modify this setting. See https://docs.anythingllm.com/configuration#anythingllm-hub-agent-skills",
    });
  }
  next();
}

/**
 * Fetch the bundle item from the community hub.
 * Sets `response.locals.bundleItem` and `response.locals.bundleUrl`.
 */
async function communityHubItem(request, response, next) {
  const { importId } = reqBody(request);
  if (!importId)
    return response.status(500).json({
      success: false,
      error: "Import ID is required",
    });

  const {
    url,
    item,
    error: fetchError,
  } = await CommunityHub.getBundleItem(importId);
  if (fetchError)
    return response.status(500).json({
      success: false,
      error: fetchError,
    });

  response.locals.bundleItem = item;
  response.locals.bundleUrl = url;
  next();
}

module.exports = {
  communityHubItem,
  communityHubDownloadsEnabled,
};
