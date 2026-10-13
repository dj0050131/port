import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";

const CommunityHub = {
  
  getItemFromImportId: async (importId) => {
    return await fetch(`${API_BASE}/community-hub/item`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify({ importId }),
    })
      .then((res) => res.json())
      .catch((e) => {
        console.error(e);
        return {
          error: e.message,
          item: null,
        };
      });
  },

  
  applyItem: async (importId, options = {}) => {
    return await fetch(`${API_BASE}/community-hub/apply`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify({ importId, options }),
    })
      .then((res) => res.json())
      .catch((e) => {
        console.error(e);
        return {
          success: false,
          error: e.message,
        };
      });
  },

  
  importBundleItem: async (importId) => {
    return await fetch(`${API_BASE}/community-hub/import`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify({ importId }),
    })
      .then(async (res) => {
        const response = await res.json();
        if (!res.ok) throw new Error(response?.error ?? res.statusText);
        return response;
      })
      .catch((e) => {
        return {
          error: e.message,
          item: null,
        };
      });
  },

  
  updateSettings: async (data) => {
    return await fetch(`${API_BASE}/community-hub/settings`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify(data),
    })
      .then(async (res) => {
        const response = await res.json();
        if (!res.ok)
          throw new Error(response.error || "Failed to update settings");
        return { success: true, error: null };
      })
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },

  
  getSettings: async () => {
    return await fetch(`${API_BASE}/community-hub/settings`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then(async (res) => {
        const response = await res.json();
        if (!res.ok)
          throw new Error(response.error || "Failed to fetch settings");
        return { connectionKey: response.connectionKey, error: null };
      })
      .catch((e) => ({
        connectionKey: null,
        error: e.message,
      }));
  },

  
  fetchExploreItems: async () => {
    return await fetch(`${API_BASE}/community-hub/explore`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch((e) => {
        console.error(e);
        return {
          success: false,
          error: e.message,
          result: null,
        };
      });
  },

  
  fetchUserItems: async () => {
    return await fetch(`${API_BASE}/community-hub/items`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch((e) => {
        console.error(e);
        return {
          success: false,
          error: e.message,
          createdByMe: {},
          teamItems: [],
        };
      });
  },

  
  createSystemPrompt: async (data) => {
    return await fetch(`${API_BASE}/community-hub/system-prompt/create`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify(data),
    })
      .then(async (res) => {
        const response = await res.json();
        if (!res.ok)
          throw new Error(response.error || "Failed to create system prompt");
        return { success: true, error: null, itemId: response.item?.id };
      })
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },

  
  createAgentFlow: async (data) => {
    return await fetch(`${API_BASE}/community-hub/agent-flow/create`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify(data),
    }).then(async (res) => {
      const response = await res.json();
      if (!res.ok)
        throw new Error(response.error || "Failed to create agent flow");
      return { success: true, error: null, itemId: response.item?.id };
    });
  },

  
  createSlashCommand: async (data) => {
    return await fetch(`${API_BASE}/community-hub/slash-command/create`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify(data),
    })
      .then(async (res) => {
        const response = await res.json();
        if (!res.ok)
          throw new Error(response.error || "Failed to create slash command");
        return { success: true, error: null, itemId: response.item?.id };
      })
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },
};

export default CommunityHub;
