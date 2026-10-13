const { SystemSettings } = require("../../models/systemSettings");

/**
 * Checks if simple SSO is enabled for issuance of temporary auth tokens.
 * Note: This middleware must be called after `validApiKey`.
 * @param {import("express").Request} request
 * @param {import("express").Response} response
 * @param {import("express").NextFunction} next
 * @returns {void}
 */
async function simpleSSOEnabled(_, response, next) {
  if (!("SIMPLE_SSO_ENABLED" in process.env)) {
    return response
      .status(403)
      .send(
        "Simple SSO is not enabled. It must be enabled to validate or issue temporary auth tokens."
      );
  }

  if (!("multiUserMode" in response.locals)) {
    const multiUserMode = await SystemSettings.isMultiUserMode();
    response.locals.multiUserMode = multiUserMode;
  }

  if (!response.locals.multiUserMode) {
    return response
      .status(403)
      .send(
        "Multi-User mode is not enabled. It must be enabled to use Simple SSO."
      );
  }

  next();
}

/**
 * Checks if simple SSO login is disabled by checking if the
 * SIMPLE_SSO_NO_LOGIN environment variable is set as well as
 * SIMPLE_SSO_ENABLED is set.
 *
 * This check should only be run when in multi-user mode when used.
 * @returns {boolean}
 */
function simpleSSOLoginDisabled() {
  return (
    "SIMPLE_SSO_ENABLED" in process.env && "SIMPLE_SSO_NO_LOGIN" in process.env
  );
}


async function simpleSSOLoginDisabledMiddleware(_, response, next) {
  if (!("multiUserMode" in response.locals)) {
    const multiUserMode = await SystemSettings.isMultiUserMode();
    response.locals.multiUserMode = multiUserMode;
  }

  if (response.locals.multiUserMode && simpleSSOLoginDisabled()) {
    response.status(403).json({
      success: false,
      error: "Login via credentials has been disabled by the administrator.",
    });
    return;
  }
  next();
}

module.exports = {
  simpleSSOEnabled,
  simpleSSOLoginDisabled,
  simpleSSOLoginDisabledMiddleware,
};
