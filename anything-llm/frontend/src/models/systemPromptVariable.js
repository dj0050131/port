import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";



const SystemPromptVariable = {
  
  getAll: async function () {
    try {
      return await fetch(`${API_BASE}/system/prompt-variables`, {
        method: "GET",
        headers: baseHeaders(),
      })
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error fetching system prompt variables:", error);
          return { variables: [] };
        });
    } catch (error) {
      console.error("Error fetching system prompt variables:", error);
      return { variables: [] };
    }
  },

  
  create: async function (variable = {}) {
    try {
      return await fetch(`${API_BASE}/system/prompt-variables`, {
        method: "POST",
        headers: baseHeaders(),
        body: JSON.stringify(variable),
      })
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error creating system prompt variable:", error);
          return { success: false, error };
        });
    } catch (error) {
      console.error("Error creating system prompt variable:", error);
      return { success: false, error };
    }
  },

  
  update: async function (id, variable = {}) {
    try {
      return await fetch(`${API_BASE}/system/prompt-variables/${id}`, {
        method: "PUT",
        headers: baseHeaders(),
        body: JSON.stringify(variable),
      })
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error updating system prompt variable:", error);
          return { success: false, error };
        });
    } catch (error) {
      console.error("Error updating system prompt variable:", error);
      return { success: false, error };
    }
  },

  
  delete: async function (id = null) {
    try {
      if (id === null) return { success: false, error: "ID is required" };
      return await fetch(`${API_BASE}/system/prompt-variables/${id}`, {
        method: "DELETE",
        headers: baseHeaders(),
      })
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error deleting system prompt variable:", error);
          return { success: false, error };
        });
    } catch (error) {
      console.error("Error deleting system prompt variable:", error);
      return { success: false, error };
    }
  },
};

export default SystemPromptVariable;
