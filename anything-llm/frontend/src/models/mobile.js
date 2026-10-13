import { API_BASE } from "@/utils/constants";
import { baseHeaders } from "@/utils/request";



const MobileConnection = {
  
  getConnectionInfo: async function () {
    return await fetch(`${API_BASE}/mobile/connect-info`, {
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch(() => false);
  },

  
  getDevices: async function () {
    return await fetch(`${API_BASE}/mobile/devices`, {
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .then((res) => res.devices || [])
      .catch(() => []);
  },

  
  deleteDevice: async function (id) {
    return await fetch(`${API_BASE}/mobile/${id}`, {
      method: "DELETE",
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch(() => false);
  },

  
  updateDevice: async function (id, updates = {}) {
    return await fetch(`${API_BASE}/mobile/update/${id}`, {
      method: "POST",
      body: JSON.stringify(updates),
      headers: baseHeaders(),
    })
      .then((res) => res.json())
      .catch(() => false);
  },
};

export default MobileConnection;
