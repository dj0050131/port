import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";

const MCPServers = {
  
  forceReload: async () => {
    return await fetch(`${API_BASE}/mcp-servers/force-reload`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch((e) => ({
        servers: [],
        success: false,
        error: e.message,
      }));
  },

  
  listServers: async () => {
    return await fetch(`${API_BASE}/mcp-servers/list`, {
      method: "GET",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
        servers: [],
      }));
  },

  
  toggleServer: async (name) => {
    return await fetch(`${API_BASE}/mcp-servers/toggle`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify({ name }),
    })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },

  
  deleteServer: async (name) => {
    return await fetch(`${API_BASE}/mcp-servers/delete`, {
      method: "POST",
      headers: baseHeaders(),
      body: JSON.stringify({ name }),
    })
      .then((res) => res.json())
      .catch((e) => ({
        success: false,
        error: e.message,
      }));
  },
};

export default MCPServers;
