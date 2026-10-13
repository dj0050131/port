const { liveSyncEndpoints } = require("./liveSync");
const { importedAgentPluginEndpoints } = require("./imported-agent-plugins");

function experimentalEndpoints(router) {
  liveSyncEndpoints(router);
  importedAgentPluginEndpoints(router);
}

module.exports = { experimentalEndpoints };
