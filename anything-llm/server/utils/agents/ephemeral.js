const AIbitat = require("./aibitat");
const AgentPlugins = require("./aibitat/plugins");
const ImportedPlugin = require("./imported");
const MCPCompatibilityLayer = require("../MCP");
const { AgentFlows } = require("../agentFlows");
const { httpSocket } = require("./aibitat/plugins/http-socket.js");
const { User } = require("../../models/user");
const { WorkspaceChats } = require("../../models/workspaceChats");
const { safeJsonParse } = require("../http");
const {
  USER_AGENT,
  WORKSPACE_AGENT,
  agentSkillsFromSystemSettings,
} = require("./defaults");
const { AgentHandler } = require(".");
const {
  WorkspaceAgentInvocation,
} = require("../../models/workspaceAgentInvocation");

/**
 * This is an instance and functional Agent handler, but it does not utilize
 * sessions or websocket's and is instead a singular one-off agent run that does
 * not persist between invocations
 */
class EphemeralAgentHandler extends AgentHandler {
  /** @type {string|null} the unique identifier for the agent invocation */
  #invocationUUID = null;
  
  #workspace = null;
  
  #userId = null;
  
  #threadId = null;
  /** @type {string|null} the session id to use for the agent */
  #sessionId = null;
  /** @type {string|null} the prompt to use for the agent */
  #prompt = null;
  
  #funcsToLoad = [];

  /** @type {AIbitat|null} */
  aibitat = null;
  /** @type {string|null} */
  channel = null;
  /** @type {string|null} */
  provider = null;
  /** @type {string|null} the model to use for the agent */
  model = null;

  
  constructor({
    uuid,
    workspace,
    prompt,
    userId = null,
    threadId = null,
    sessionId = null,
  }) {
    super({ uuid });
    this.#invocationUUID = uuid;
    this.#workspace = workspace;
    this.#prompt = prompt;

    // Note: userId for ephemeral agent is only available
    // via the workspace-thread chat endpoints for the API
    this.#userId = userId;
    this.#threadId = threadId;
    this.#sessionId = sessionId;
  }

  log(text, ...args) {
  }

  closeAlert() {
    this.log(`End ${this.#invocationUUID}::${this.provider}:${this.model}`);
  }

  async #chatHistory(limit = 10) {
    try {
      const rawHistory = (
        await WorkspaceChats.where(
          {
            workspaceId: this.#workspace.id,
            user_id: this.#userId || null,
            thread_id: this.#threadId || null,
            api_session_id: this.#sessionId,
            include: true,
          },
          limit,
          { id: "desc" }
        )
      ).reverse();

      const agentHistory = [];
      rawHistory.forEach((chatLog) => {
        agentHistory.push(
          {
            from: USER_AGENT.name,
            to: WORKSPACE_AGENT.name,
            content: chatLog.prompt,
            state: "success",
          },
          {
            from: WORKSPACE_AGENT.name,
            to: USER_AGENT.name,
            content: safeJsonParse(chatLog.response)?.text || "",
            state: "success",
          }
        );
      });
      return agentHistory;
    } catch (e) {
      this.log("Error loading chat history", e.message);
      return [];
    }
  }

  /**
   * Attempts to find a fallback provider and model to use if the workspace
   * does not have an explicit `agentProvider` and `agentModel` set.
   * 1. Fallback to the workspace `chatProvider` and `chatModel` if they exist.
   * 2. Fallback to the system `LLM_PROVIDER` and try to load the associated default model via ENV params or a base available model.
   * 3. Otherwise, return null - will likely throw an error the user can act on.
   * @returns {object|null} - An object with provider and model keys.
   */
  #getFallbackProvider() {
    if (this.#workspace.chatProvider && this.#workspace.chatModel) {
      return {
        provider: this.#workspace.chatProvider,
        model: this.#workspace.chatModel,
      };
    }

    const systemProvider = process.env.LLM_PROVIDER;
    const systemModel = this.providerDefault(systemProvider);
    if (systemProvider && systemModel) {
      return {
        provider: systemProvider,
        model: systemModel,
      };
    }

    return null;
  }

  /**
   * Finds or assumes the model preference value to use for API calls.
   * If multi-model loading is supported, we use their agent model selection of the workspace
   * If not supported, we attempt to fallback to the system provider value for the LLM preference
   * and if that fails - we assume a reasonable base model to exist.
   * @returns {string|null} the model preference value to use in API calls
   */
  #fetchModel() {
    // Provider was not explicitly set for workspace, so we are going to run our fallback logic
    if (!this.provider) {
      const fallback = this.#getFallbackProvider();
      if (!fallback) throw new Error("No valid provider found for the agent.");
      this.provider = fallback.provider; // re-set the provider to the fallback provider so it is not null.
      return fallback.model; // set its defined model based on fallback logic.
    }

    if (this.#workspace.agentModel) return this.#workspace.agentModel;

    // Otherwise, we have no model to use - so guess a default model to use via the provider
    return this.providerDefault();
  }

  #providerSetupAndCheck() {
    this.provider = this.#workspace.agentProvider ?? null;
    this.model = this.#fetchModel();

    if (!this.provider)
      throw new Error("No valid provider found for the agent.");
    this.log(`Start ${this.#invocationUUID}::${this.provider}:${this.model}`);
    this.checkSetup();
  }

  async #attachPlugins(args) {
    for (const name of this.#funcsToLoad) {
      // Load child plugin
      if (name.includes("#")) {
        const [parent, childPluginName] = name.split("#");
        if (!AgentPlugins.hasOwnProperty(parent)) {
          this.log(
            `${parent} is not a valid plugin. Skipping inclusion to agent cluster.`
          );
          continue;
        }

        const childPlugin = AgentPlugins[parent].plugin.find(
          (child) => child.name === childPluginName
        );
        if (!childPlugin) {
          this.log(
            `${parent} does not have child plugin named ${childPluginName}. Skipping inclusion to agent cluster.`
          );
          continue;
        }

        const callOpts = this.parseCallOptions(
          args,
          childPlugin?.startupConfig?.params,
          name
        );
        this.aibitat.use(childPlugin.plugin(callOpts));
        this.log(
          `Attached ${parent}:${childPluginName} plugin to Agent cluster`
        );
        continue;
      }

      if (name.startsWith("@@flow_")) {
        const uuid = name.replace("@@flow_", "");
        const plugin = AgentFlows.loadFlowPlugin(uuid, this.aibitat);
        if (!plugin) {
          this.log(
            `Flow ${uuid} not found in flows directory. Skipping inclusion to agent cluster.`
          );
          continue;
        }

        this.aibitat.use(plugin.plugin());
        this.log(
          `Attached flow ${plugin.name} (${plugin.flowName}) plugin to Agent cluster`
        );
        continue;
      }

      // All sub-tools are loaded here and are denoted by `pluginName:toolName` as their identifier.
      // This will replace the parent MCP server plugin with the sub-tools as child plugins so they
      // Since to get to this point, the `activeMCPServers` method has already been called, we can
      if (name.startsWith("@@mcp_")) {
        const mcpPluginName = name.replace("@@mcp_", "");
        const plugins =
          await new MCPCompatibilityLayer().convertServerToolsToPlugins(
            mcpPluginName,
            this.aibitat
          );
        if (!plugins) {
          this.log(
            `MCP ${mcpPluginName} not found in MCP server config. Skipping inclusion to agent cluster.`
          );
          continue;
        }

        // Remove the old function from the agent functions directly
        this.aibitat.agents.get("@agent").functions = this.aibitat.agents
          .get("@agent")
          .functions.filter((f) => f.name !== name);
        for (const plugin of plugins)
          this.aibitat.agents.get("@agent").functions.push(plugin.name);

        plugins.forEach((plugin) => {
          this.aibitat.use(plugin.plugin());
          this.log(
            `Attached MCP::${plugin.toolName} MCP tool to Agent cluster`
          );
        });
        continue;
      }

      if (name.startsWith("@@")) {
        const hubId = name.replace("@@", "");
        const valid = ImportedPlugin.validateImportedPluginHandler(hubId);
        if (!valid) {
          this.log(
            `Imported plugin by hubId ${hubId} not found in plugin directory. Skipping inclusion to agent cluster.`
          );
          continue;
        }

        const plugin = ImportedPlugin.loadPluginByHubId(hubId);
        const callOpts = plugin.parseCallOptions();
        this.aibitat.use(plugin.plugin(callOpts));
        this.log(
          `Attached ${plugin.name} (${hubId}) imported plugin to Agent cluster`
        );
        continue;
      }

      if (!AgentPlugins.hasOwnProperty(name)) {
        this.log(
          `${name} is not a valid plugin. Skipping inclusion to agent cluster.`
        );
        continue;
      }

      const callOpts = this.parseCallOptions(
        args,
        AgentPlugins[name].startupConfig.params
      );
      const AIbitatPlugin = AgentPlugins[name];
      this.aibitat.use(AIbitatPlugin.plugin(callOpts));
      this.log(`Attached ${name} plugin to Agent cluster`);
    }
  }

  async #loadAgents() {
    // Default User agent and workspace agent
    this.log(`Attaching user and default agent to Agent cluster.`);
    this.aibitat.agent(USER_AGENT.name, USER_AGENT.getDefinition());
    const user = this.#userId
      ? await User.get({ id: Number(this.#userId) })
      : null;

    this.aibitat.agent(
      WORKSPACE_AGENT.name,
      await WORKSPACE_AGENT.getDefinition(this.provider, this.#workspace, user)
    );

    this.#funcsToLoad = [
      ...(await agentSkillsFromSystemSettings()),
      ...ImportedPlugin.activeImportedPlugins(),
      ...AgentFlows.activeFlowPlugins(),
      ...(await new MCPCompatibilityLayer().activeMCPServers()),
    ];
  }

  async init() {
    this.#providerSetupAndCheck();
    return this;
  }

  async createAIbitat(
    args = {
      handler,
    }
  ) {
    this.aibitat = new AIbitat({
      provider: this.provider ?? "openai",
      model: this.model ?? "gpt-4o",
      chats: await this.#chatHistory(20),
      handlerProps: {
        invocation: {
          workspace: this.#workspace,
          workspace_id: this.#workspace.id,
        },
        log: this.log,
      },
    });

    this.log(`Attached ${httpSocket.name} plugin to Agent cluster`);
    this.aibitat.use(
      httpSocket.plugin({
        handler: args.handler,
        muteUserReply: true,
        introspection: true,
      })
    );

    await this.#loadAgents();

    await this.#attachPlugins(args);
  }

  startAgentCluster() {
    return this.aibitat.start({
      from: USER_AGENT.name,
      to: this.channel ?? WORKSPACE_AGENT.name,
      content: this.#prompt,
    });
  }

  
  static isAgentInvocation({ message }) {
    const agentHandles = WorkspaceAgentInvocation.parseAgents(message);
    if (agentHandles.length > 0) return true;
    return false;
  }
}

const EventEmitter = require("node:events");
const { writeResponseChunk } = require("../helpers/chat/responses");

/**
 * This is a special EventEmitter specifically used in the Aibitat agent handler
 * that enables us to use HTTP to relay all .introspect and .send events back to an
 * http handler instead of websockets, like we do on the frontend. This interface is meant to
 * mock a websocket interface for the methods used and bind them to an HTTP method so that the developer
 * API can invoke agent calls.
 */
class EphemeralEventListener extends EventEmitter {
  messages = [];
  constructor() {
    super();
  }

  send(jsonData) {
    const data = JSON.parse(jsonData);
    this.messages.push(data);
    this.emit("chunk", data);
  }

  close() {
    this.emit("closed");
  }

  /**
   * Compacts all messages in class and returns them in a condensed format.
   * @returns {{thoughts: string[], textResponse: string}}
   */
  packMessages() {
    const thoughts = [];
    let textResponse = null;
    for (let msg of this.messages) {
      if (msg.type !== "statusResponse") {
        textResponse = msg.content;
      } else {
        thoughts.push(msg.content);
      }
    }
    return { thoughts, textResponse };
  }

  
  async waitForClose() {
    return new Promise((resolve) => {
      this.once("closed", () => resolve(this.packMessages()));
    });
  }

  /**
   * Streams the events with `writeResponseChunk` over HTTP chunked encoding
   * and returns on the close event emission.
   * ----------
   * DevNote: Agents do not stream so in here we are simply
   * emitting the thoughts and text response as soon as we get them.
   * @param {import("express").Response} response
   * @param {string} uuid - Unique identifier that is the same across chunks.
   * @returns {Promise<{thoughts: string[], textResponse: string}>}
   */
  async streamAgentEvents(response, uuid) {
    const onChunkHandler = (data) => {
      if (data.type === "statusResponse") {
        return writeResponseChunk(response, {
          id: uuid,
          type: "agentThought",
          thought: data.content,
          sources: [],
          attachments: [],
          close: false,
          error: null,
          animate: true,
        });
      }

      return writeResponseChunk(response, {
        id: uuid,
        type: "textResponse",
        textResponse: data.content,
        sources: [],
        attachments: [],
        close: true,
        error: null,
        animate: false,
      });
    };
    this.on("chunk", onChunkHandler);

    return this.waitForClose().then((closedResponse) => {
      this.removeListener("chunk", onChunkHandler);
      return closedResponse;
    });
  }
}

module.exports = { EphemeralAgentHandler, EphemeralEventListener };
