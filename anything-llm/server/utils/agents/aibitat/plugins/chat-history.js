const { WorkspaceChats } = require("../../../../models/workspaceChats");

/**
 * Plugin to save chat history to AnythingLLM DB.
 */
const chatHistory = {
  name: "chat-history",
  startupConfig: {
    params: {},
  },
  plugin: function () {
    return {
      name: this.name,
      setup: function (aibitat) {
        aibitat.onMessage(async () => {
          try {
            const lastResponses = aibitat.chats.slice(-2);
            if (lastResponses.length !== 2) return;
            const [prev, last] = lastResponses;

            // We need a full conversation reply with prev being from
            if (prev.from !== "USER" || last.from === "USER") return;

            // If we have a post-reply flow we should save the chat using this special flow
            if (aibitat.hasOwnProperty("_replySpecialAttributes")) {
              await this._storeSpecial(aibitat, {
                prompt: prev.content,
                response: last.content,
                options: aibitat._replySpecialAttributes,
              });
              delete aibitat._replySpecialAttributes;
              return;
            }

            await this._store(aibitat, {
              prompt: prev.content,
              response: last.content,
            });
          } catch {}
        });
      },
      _store: async function (aibitat, { prompt, response } = {}) {
        const invocation = aibitat.handlerProps.invocation;
        await WorkspaceChats.new({
          workspaceId: Number(invocation.workspace_id),
          prompt,
          response: {
            text: response,
            sources: [],
            type: "chat",
          },
          user: { id: invocation?.user_id || null },
          threadId: invocation?.thread_id || null,
        });
      },
      _storeSpecial: async function (
        aibitat,
        { prompt, response, options = {} } = {}
      ) {
        const invocation = aibitat.handlerProps.invocation;
        await WorkspaceChats.new({
          workspaceId: Number(invocation.workspace_id),
          prompt,
          response: {
            sources: options?.sources ?? [],
            text: options.hasOwnProperty("storedResponse")
              ? options.storedResponse(response)
              : response,
            type: options?.saveAsType ?? "chat",
          },
          user: { id: invocation?.user_id || null },
          threadId: invocation?.thread_id || null,
        });
        options?.postSave();
      },
    };
  },
};

module.exports = { chatHistory };
