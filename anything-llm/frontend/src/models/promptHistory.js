import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";



const PromptHistory = {
  
  forWorkspace: async function (workspaceId) {
    try {
      return await fetch(
        `${API_BASE}/workspace/${workspaceId}/prompt-history`,
        {
          method: "GET",
          headers: baseHeaders(),
        }
      )
        .then((res) => res.json())
        .then((res) => res.history || [])
        .catch((error) => {
          console.error("Error fetching prompt history:", error);
          return [];
        });
    } catch (error) {
      console.error("Error fetching prompt history:", error);
      return [];
    }
  },

  
  clearAll: async function (workspaceId) {
    try {
      return await fetch(
        `${API_BASE}/workspace/${workspaceId}/prompt-history`,
        {
          method: "DELETE",
          headers: baseHeaders(),
        }
      )
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error clearing prompt history:", error);
          return { success: false, error };
        });
    } catch (error) {
      console.error("Error clearing prompt history:", error);
      return { success: false, error };
    }
  },

  delete: async function (id) {
    try {
      return await fetch(`${API_BASE}/workspace/prompt-history/${id}`, {
        method: "DELETE",
        headers: baseHeaders(),
      })
        .then((res) => res.json())
        .catch((error) => {
          console.error("Error deleting prompt history:", error);
          return { success: false, error };
        });
    } catch (error) {
      console.error("Error deleting prompt history:", error);
      return { success: false, error };
    }
  },
};

export default PromptHistory;
