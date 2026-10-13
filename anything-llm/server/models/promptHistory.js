const prisma = require("../utils/prisma");

const PromptHistory = {
  new: async function ({ workspaceId, prompt, modifiedBy = null }) {
    try {
      const history = await prisma.prompt_history.create({
        data: {
          workspaceId: Number(workspaceId),
          prompt: String(prompt),
          modifiedBy: !!modifiedBy ? Number(modifiedBy) : null,
        },
      });
      return { history, message: null };
    } catch (error) {
      console.error(error.message);
      return { history: null, message: error.message };
    }
  },

  
  forWorkspace: async function (
    workspaceId = null,
    limit = null,
    orderBy = null
  ) {
    if (!workspaceId) return [];
    try {
      const history = await prisma.prompt_history.findMany({
        where: { workspaceId: Number(workspaceId) },
        ...(limit !== null ? { take: limit } : {}),
        ...(orderBy !== null
          ? { orderBy }
          : { orderBy: { modifiedAt: "desc" } }),
        include: {
          user: {
            select: {
              username: true,
            },
          },
        },
      });
      return history;
    } catch (error) {
      console.error(error.message);
      return [];
    }
  },

  get: async function (clause = {}, limit = null, orderBy = null) {
    try {
      const history = await prisma.prompt_history.findFirst({
        where: clause,
        ...(limit !== null ? { take: limit } : {}),
        ...(orderBy !== null ? { orderBy } : {}),
        include: {
          user: {
            select: {
              id: true,
              username: true,
              role: true,
            },
          },
        },
      });
      return history || null;
    } catch (error) {
      console.error(error.message);
      return null;
    }
  },

  delete: async function (clause = {}) {
    try {
      await prisma.prompt_history.deleteMany({ where: clause });
      return true;
    } catch (error) {
      console.error(error.message);
      return false;
    }
  },

  
  handlePromptChange: async function (workspaceData, user = null) {
    try {
      await this.new({
        workspaceId: workspaceData.id,
        prompt: workspaceData.openAiPrompt, // Store previous prompt as history
        modifiedBy: user?.id,
      });
    } catch (error) {
      console.error("Failed to create prompt history:", error.message);
    }
  },
};

module.exports = { PromptHistory };
