const AgentPlugins = require("./aibitat/plugins");
const { SystemSettings } = require("../../models/systemSettings");
const { safeJsonParse } = require("../http");
const Provider = require("./aibitat/providers/ai-provider");
const ImportedPlugin = require("./imported");
const { AgentFlows } = require("../agentFlows");
const MCPCompatibilityLayer = require("../MCP");
const { SystemPromptVariables } = require("../../models/systemPromptVariables");

const DEFAULT_SKILLS = [
  AgentPlugins.memory.name,
  AgentPlugins.docSummarizer.name,
  AgentPlugins.webScraping.name,
];

const USER_AGENT = {
  name: "USER",
  getDefinition: () => {
    return {
      interrupt: "ALWAYS",
      role: "I am the human monitor and oversee this chat. Any questions on action or decision making should be directed to me.",
    };
  },
};

const WORKSPACE_AGENT = {
  name: "@agent",
  
  getDefinition: async (provider = null, workspace = null, user = null) => {
    return {
      role: await Provider.systemPrompt({ provider, workspace, user }),
      functions: [
        ...(await agentSkillsFromSystemSettings()),
        ...ImportedPlugin.activeImportedPlugins(),
        ...AgentFlows.activeFlowPlugins(),
        ...(await new MCPCompatibilityLayer().activeMCPServers()),
      ],
    };
  },
};


async function agentSkillsFromSystemSettings() {
  const systemFunctions = [];

  const _disabledDefaultSkills = safeJsonParse(
    await SystemSettings.getValueOrFallback(
      { label: "disabled_agent_skills" },
      "[]"
    ),
    []
  );
  DEFAULT_SKILLS.forEach((skill) => {
    if (!_disabledDefaultSkills.includes(skill))
      systemFunctions.push(AgentPlugins[skill].name);
  });

  const _setting = safeJsonParse(
    await SystemSettings.getValueOrFallback(
      { label: "default_agent_skills" },
      "[]"
    ),
    []
  );
  _setting.forEach((skillName) => {
    if (!AgentPlugins.hasOwnProperty(skillName)) return;

    // This is a plugin module with many sub-children plugins who
    if (Array.isArray(AgentPlugins[skillName].plugin)) {
      for (const subPlugin of AgentPlugins[skillName].plugin) {
        systemFunctions.push(
          `${AgentPlugins[skillName].name}#${subPlugin.name}`
        );
      }
      return;
    }

    // This is normal single-stage plugin
    systemFunctions.push(AgentPlugins[skillName].name);
  });
  return systemFunctions;
}

module.exports = {
  USER_AGENT,
  WORKSPACE_AGENT,
  agentSkillsFromSystemSettings,
};
