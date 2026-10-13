import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";

const AgentFlows = {
  
  saveFlow: async (name, config, uuid = null) => {
    return await fetch(`${API_BASE}/agent-flows/save`, {
      method: "POST",
      headers: {
        ...baseHeaders(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ name, config, uuid }),
    })
      .then((res) => {
        if (!res.ok) throw new Error(res.error || "Failed to save flow");
        return res;
      })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
        flow: null,
      }));
  },

  
  listFlows: async () => {
    return await fetch(`${API_BASE}/agent-flows/list`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
        flows: [],
      }));
  },

  
  getFlow: async (uuid) => {
    return await fetch(`${API_BASE}/agent-flows/${uuid}`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => {
        if (!res.ok) throw new Error(response.error || "Failed to get flow");
        return res;
      })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
        flow: null,
      }));
  },

  
  //     method: "POST",
  //       "Content-Type": "application/json",
  //     },
  //     body: JSON.stringify({ variables }),
  //     .then((res) => res.json())
  //       success: false,
  //       results: null,
  //     }));
  // },

  
  deleteFlow: async (uuid) => {
    return await fetch(`${API_BASE}/agent-flows/${uuid}`, {
      method: "DELETE",
      headers: baseHeaders(),
    })
      .then((res) => {
        if (!res.ok) throw new Error(response.error || "Failed to delete flow");
        return res;
      })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },

  
  toggleFlow: async (uuid, active) => {
    try {
      const result = await fetch(`${API_BASE}/agent-flows/${uuid}/toggle`, {
        method: "POST",
        headers: {
          ...baseHeaders(),
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ active }),
      })
        .then((res) => {
          if (!res.ok) throw new Error(res.error || "Failed to toggle flow");
          return res;
        })
        .then((res) => res.json());
      return { success: true, flow: result.flow };
    } catch (error) {
      console.error("Failed to toggle flow:", error);
      return { success: false, error: error.message };
    }
  },
};

export default AgentFlows;
