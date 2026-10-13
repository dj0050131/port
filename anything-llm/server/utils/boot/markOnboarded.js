const { SystemSettings } = require("../../models/systemSettings");

/**
 * Mark the onboarding as completed for legacy users prior to this change where onboarding is now a flag in the DB.
 * This is a legacy patch to ensure that existing users are not redirected to the onboarding page who have been using the app for a while.
 */
async function markOnboarded() {
  try {
    const onboardingStatus = await SystemSettings.isOnboardingComplete();
    if (onboardingStatus === true) return;

    const alreadyOnboarded = await isLegacyOnboarded();
    if (alreadyOnboarded === true) {
      await SystemSettings.markOnboardingComplete();
      return true;
    }
    return false;
  } catch (e) {
    console.error(
      "\x1b[31m[ONBOARDING PATCH]\x1b[0m Error marking onboarding as complete",
      e.message,
      e
    );
    return false;
  }
}


async function isLegacyOnboarded() {
  if (!!process.env.LLM_PROVIDER) return true;

  if (!!process.env.VECTOR_DB) return true;

  if (!!process.env.AUTH_TOKEN || !!process.env.JWT_SECRET) return true;

  if ((await SystemSettings.isMultiUserMode()) === true) return true;
  return false;
}

module.exports = markOnboarded;
