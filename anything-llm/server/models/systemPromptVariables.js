const prisma = require("../utils/prisma");
const moment = require("moment");



const SystemPromptVariables = {
  VALID_TYPES: ["user", "workspace", "system", "static"],
  DEFAULT_VARIABLES: [
    {
      key: "time",
      value: () => moment().format("LTS"),
      description: "Current time",
      type: "system",
      multiUserRequired: false,
    },
    {
      key: "date",
      value: () => moment().format("LL"),
      description: "Current date",
      type: "system",
      multiUserRequired: false,
    },
    {
      key: "datetime",
      value: () => moment().format("LLLL"),
      description: "Current date and time",
      type: "system",
      multiUserRequired: false,
    },
    {
      key: "user.id",
      value: (userId = null) => {
        if (!userId) return "[User ID]";
        return userId;
      },
      description: "Current user's ID",
      type: "user",
      multiUserRequired: true,
    },
    {
      key: "user.name",
      value: async (userId = null) => {
        if (!userId) return "[User name]";
        try {
          const user = await prisma.users.findUnique({
            where: { id: Number(userId) },
            select: { username: true },
          });
          return user?.username || "[User name is empty or unknown]";
        } catch (error) {
          console.error("Error fetching user name:", error);
          return "[User name is empty or unknown]";
        }
      },
      description: "Current user's username",
      type: "user",
      multiUserRequired: true,
    },
    {
      key: "user.bio",
      value: async (userId = null) => {
        if (!userId) return "[User bio]";
        try {
          const user = await prisma.users.findUnique({
            where: { id: Number(userId) },
            select: { bio: true },
          });
          return user?.bio || "[User bio is empty]";
        } catch (error) {
          console.error("Error fetching user bio:", error);
          return "[User bio is empty]";
        }
      },
      description: "Current user's bio field from their profile",
      type: "user",
      multiUserRequired: true,
    },
    {
      key: "workspace.id",
      value: (workspaceId = null) => {
        if (!workspaceId) return "[Workspace ID]";
        return workspaceId;
      },
      description: "Current workspace's ID",
      type: "workspace",
      multiUserRequired: false,
    },
    {
      key: "workspace.name",
      value: async (workspaceId = null) => {
        if (!workspaceId) return "[Workspace name]";
        const workspace = await prisma.workspaces.findUnique({
          where: { id: Number(workspaceId) },
          select: { name: true },
        });
        return workspace?.name || "[Workspace name is empty or unknown]";
      },
      description: "Current workspace's name",
      type: "workspace",
      multiUserRequired: false,
    },
  ],

  
  get: async function (key = null) {
    if (!key) return null;
    const variable = await prisma.system_prompt_variables.findUnique({
      where: { key: String(key) },
    });
    return variable;
  },

  
  getAll: async function (userId = null) {
    const userDefinedSystemVariables =
      await prisma.system_prompt_variables.findMany();
    const formattedDbVars = userDefinedSystemVariables.map((v) => ({
      id: v.id,
      key: v.key,
      value: v.value,
      description: v.description,
      type: v.type,
      userId: v.userId,
    }));

    // If userId is not provided, filter the default variables to only include non-multiUserRequired variables
    const defaultSystemVariables = !userId
      ? this.DEFAULT_VARIABLES.filter((v) => !v.multiUserRequired)
      : this.DEFAULT_VARIABLES;

    return [...defaultSystemVariables, ...formattedDbVars];
  },

  
  create: async function ({
    key,
    value,
    description = null,
    type = "static",
    userId = null,
  }) {
    await this._checkVariableKey(key, true);
    return await prisma.system_prompt_variables.create({
      data: {
        key: String(key),
        value: String(value),
        description: description ? String(description) : null,
        type: type ? String(type) : "static",
        userId: userId ? Number(userId) : null,
      },
    });
  },

  
  update: async function (id, { key, value, description = null }) {
    if (!id || !key || !value) return null;
    const existingRecord = await prisma.system_prompt_variables.findFirst({
      where: { id: Number(id) },
    });
    if (!existingRecord) throw new Error("System prompt variable not found");
    await this._checkVariableKey(key, false);

    return await prisma.system_prompt_variables.update({
      where: { id: existingRecord.id },
      data: {
        key: String(key),
        value: String(value),
        description: description ? String(description) : null,
      },
    });
  },

  
  delete: async function (id = null) {
    try {
      await prisma.system_prompt_variables.delete({
        where: { id: Number(id) },
      });
      return true;
    } catch (error) {
      console.error("Error deleting variable:", error);
      return false;
    }
  },

  
  expandSystemPromptVariables: async function (
    str,
    userId = null,
    workspaceId = null
  ) {
    if (!str) return str;

    try {
      const allVariables = await this.getAll(userId);
      let result = str;

      const matches = str.match(/\{([^}]+)\}/g) || [];

        const key = match.substring(1, match.length - 1); // Remove { and }

        const isWorkspaceOrUserVariable = ["workspace.", "user."].some(
          (prefix) => key.startsWith(prefix)
        );

        if (isWorkspaceOrUserVariable) {
          let variableTypeDisplay;
          if (key.startsWith("workspace.")) variableTypeDisplay = "Workspace";
          else if (key.startsWith("user.")) variableTypeDisplay = "User";
          else throw new Error(`Invalid class-based variable: ${key}`);

          const variable = allVariables.find((v) => v.key === key);

          if (variable && typeof variable.value === "function") {
            if (variable.value.constructor.name === "AsyncFunction") {
              let value;
              try {
                if (variableTypeDisplay === "Workspace")
                  value = await variable.value(workspaceId);
                else if (variableTypeDisplay === "User")
                  value = await variable.value(userId);
                else throw new Error(`Invalid class-based variable: ${key}`);
              } catch (error) {
                console.error(
                  `Error processing ${variableTypeDisplay} variable ${key}:`,
                  error
                );
                value = `[${variableTypeDisplay} ${prop}]`;
              }
              result = result.replace(match, value);
            } else {
              let value;
              try {
                // Call the variable function with the appropriate workspace or user ID
                if (variableTypeDisplay === "Workspace")
                  value = variable.value(workspaceId);
                else if (variableTypeDisplay === "User")
                  value = variable.value(userId);
                else throw new Error(`Invalid class-based variable: ${key}`);
              } catch (error) {
                console.error(
                  `Error processing ${variableTypeDisplay} variable ${key}:`,
                  error
                );
                value = `[${variableTypeDisplay} ${prop}]`;
              }
              result = result.replace(match, value);
            }
          } else {
            // If the variable is not a function, replace the match with the variable value
            result = result.replace(match, `[${variableTypeDisplay} ${prop}]`);
          }
          continue;
        }

        const variable = allVariables.find((v) => v.key === key);
        if (!variable) continue;

        // For dynamic and system variables, call the function to get the current value
        if (
          ["system"].includes(variable.type) &&
          typeof variable.value === "function"
        ) {
          try {
            if (variable.value.constructor.name === "AsyncFunction") {
              const value = await variable.value(userId);
              result = result.replace(match, value);
            } else {
              const value = variable.value();
              result = result.replace(match, value);
            }
          } catch (error) {
            console.error(`Error processing dynamic variable ${key}:`, error);
            result = result.replace(match, match);
          }
        } else {
          result = result.replace(match, variable.value || match);
        }
      }
      return result;
    } catch (error) {
      console.error("Error in expandSystemPromptVariables:", error);
      return str;
    }
  },

  
  _checkVariableKey: async function (key = null, checkExisting = true) {
    if (!key) throw new Error("Key is required");
    if (typeof key !== "string") throw new Error("Key must be a string");
    if (!/^[a-zA-Z0-9_]+$/.test(key))
      throw new Error("Key must contain only letters, numbers and underscores");
    if (key.length > 255)
      throw new Error("Key must be less than 255 characters");
    if (key.length < 3) throw new Error("Key must be at least 3 characters");
    if (key.startsWith("user."))
      throw new Error("Key cannot start with 'user.'");
    if (key.startsWith("system."))
      throw new Error("Key cannot start with 'system.'");
    if (checkExisting && (await this.get(key)) !== null)
      throw new Error("System prompt variable with this key already exists");

    return true;
  },
};

module.exports = { SystemPromptVariables };
