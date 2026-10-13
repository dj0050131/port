const { getGitVersion } = require("../../endpoints/utils");
const { Telemetry } = require("../../models/telemetry");

async function setupTelemetry() {
  if (process.env.DISABLE_TELEMETRY === "true") {
    return true;
  }

  if (Telemetry.isDev()) {
    return;
  }
  await Telemetry.findOrCreateId();
  await Telemetry.sendTelemetry("server_boot", {
    commit: getGitVersion(),
  });
  return;
}

module.exports = setupTelemetry;
