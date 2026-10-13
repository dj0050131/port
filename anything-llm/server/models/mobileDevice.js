const prisma = require("../utils/prisma");
const { v4: uuidv4 } = require("uuid");
const ip = require("ip");




const TemporaryMobileDeviceRequests = new Map();

const MobileDevice = {
  platform: "server",
  validDeviceOs: ["android"],
  tablename: "desktop_mobile_devices",
  writable: ["approved"],
  validators: {
    approved: (value) => {
      if (typeof value !== "boolean") return "Must be a boolean";
      return null;
    },
  },

  /**
   * Looks up and consumes a temporary token that was registered
   * Will return null if the token is not found or expired.
   * @param {string} token - The temporary token to lookup
   * @returns {TemporaryMobileDeviceRequest|null} Temp token details
   */
  tempToken: (token = null) => {
    try {
      if (!token || !TemporaryMobileDeviceRequests.has(token)) return null;
      const tokenData = TemporaryMobileDeviceRequests.get(token);
      if (tokenData.expiresAt < Date.now()) return null;
      return tokenData;
    } catch (error) {
      return null;
    } finally {
      TemporaryMobileDeviceRequests.delete(token);
    }
  },

  /**
   * Registers a temporary token for a mobile device request
   * This is just using a random token to identify the request
   * @security Note: If we use a JWT the QR code that encodes it becomes extremely complex
   * and noisy as QR codes have byte limits that could be exceeded with JWTs. Since this is
   * a temporary token that is only used to register a device and is short lived we can use UUIDs.
   * @param {import("@prisma/client").users|null} user - User to get connection URL for in Multi-User Mode
   * @returns {string} The temporary token
   */
  registerTempToken: function (user = null) {
    let tokenData = {};
    if (user) tokenData.userId = user.id;
    else tokenData.userId = null;

    // Set short lived expiry to this mapping
    tokenData.createdAt = createdAt;
    tokenData.expiresAt = createdAt + 3 * 60_000;

    const tempToken = uuidv4().split("-").slice(0, 3).join("");
    TemporaryMobileDeviceRequests.set(tempToken, tokenData);

    this.cleanupExpiredTokens();
    return tempToken;
  },

  /**
   * Cleans up expired temporary registration tokens
   * Should run quick since this mapping is wiped often
   * and does not live past restarts.
   */
  cleanupExpiredTokens: function () {
    const now = Date.now();
    for (const [token, data] of TemporaryMobileDeviceRequests.entries()) {
      if (data.expiresAt < now) TemporaryMobileDeviceRequests.delete(token);
    }
  },

  
  connectionURL: function (user = null) {
    let baseUrl = "/api/mobile";
    if (process.env.NODE_ENV === "production") baseUrl = "/api/mobile";
    else
      baseUrl = `http://${ip.address()}:${process.env.SERVER_PORT || 3001}/api/mobile`;

    const tempToken = this.registerTempToken(user);
    baseUrl = `${baseUrl}?t=${tempToken}`;
    return baseUrl;
  },

  
  create: async function ({ deviceOs, deviceName, userId = null }) {
    try {
      if (!deviceOs || !deviceName)
        return { device: null, error: "Device OS and name are required" };
      if (!this.validDeviceOs.includes(deviceOs))
        return { device: null, error: `Invalid device OS - ${deviceOs}` };

      const device = await prisma.desktop_mobile_devices.create({
        data: {
          deviceName: String(deviceName),
          deviceOs: String(deviceOs).toLowerCase(),
          token: uuidv4(),
          userId: userId ? Number(userId) : null,
        },
      });
      return { device, error: null };
    } catch (error) {
      console.error("Failed to create mobile device", error);
      return { device: null, error: error.message };
    }
  },

  
  update: async function (id, updates = {}) {
    const device = await this.get({ id: parseInt(id) });
    if (!device) return { device: null, error: "Device not found" };

    const validUpdates = {};
    for (const [key, value] of Object.entries(updates)) {
      if (!this.writable.includes(key)) continue;
      const validation = this.validators[key](value);
      if (validation !== null) return { device: null, error: validation };
      validUpdates[key] = value;
    }
    if (Object.keys(validUpdates).length === 0) return { device, error: null };

    const updatedDevice = await prisma.desktop_mobile_devices.update({
      where: { id: device.id },
      data: validUpdates,
    });
    return { device: updatedDevice, error: null };
  },

  
  get: async function (clause = {}, include = null) {
    try {
      const device = await prisma.desktop_mobile_devices.findFirst({
        where: clause,
        ...(include !== null ? { include } : {}),
      });
      return device;
    } catch (error) {
      console.error("FAILED TO GET MOBILE DEVICE.", error);
      return [];
    }
  },

  
  delete: async function (id) {
    try {
      await prisma.desktop_mobile_devices.delete({
        where: { id: parseInt(id) },
      });
      return { success: true, error: null };
    } catch (error) {
      console.error("Failed to delete mobile device", error);
      return { success: false, error: error.message };
    }
  },

  
  where: async function (
    clause = {},
    limit = null,
    orderBy = null,
    include = null
  ) {
    try {
      const devices = await prisma.desktop_mobile_devices.findMany({
        where: clause,
        ...(limit !== null ? { take: limit } : {}),
        ...(orderBy !== null ? { orderBy } : {}),
        ...(include !== null ? { include } : {}),
      });
      return devices;
    } catch (error) {
      console.error("FAILED TO GET MOBILE DEVICES.", error.message);
      return [];
    }
  },
};

module.exports = { MobileDevice };
