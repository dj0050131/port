
export function readableType(type) {
  switch (type) {
    case "agentSkills":
    case "agentSkill":
      return "Agent Skills";
    case "systemPrompt":
    case "systemPrompts":
      return "System Prompts";
    case "slashCommand":
    case "slashCommands":
      return "Slash Commands";
    case "agentFlows":
    case "agentFlow":
      return "Agent Flows";
  }
}


export function typeToPath(type) {
  switch (type) {
    case "agentSkill":
    case "agentSkills":
      return "agent-skills";
    case "systemPrompt":
    case "systemPrompts":
      return "system-prompts";
    case "slashCommand":
    case "slashCommands":
      return "slash-commands";
    case "agentFlow":
    case "agentFlows":
      return "agent-flows";
  }
}
