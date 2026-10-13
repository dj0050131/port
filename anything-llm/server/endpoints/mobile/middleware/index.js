const { MobileDevice } = require("../../../models/mobileDevice");
const { SystemSettings } = require("../../../models/systemSettings");
const { User } = require("../../../models/user");


async function validDeviceToken(request, response, next) {
  try {
    const token = request.header("x-anythingllm-mobile-device-token");
    if (!token)
      return response.status(400).json({ error: "Device token is required" });

    const device = await MobileDevice.get(
      { token: String(token) },
      { user: true }
    );
    if (!device)
      return response.status(400).json({ error: "Device not found" });
    if (!device.approved)
      return response.status(400).json({ error: "Device not approved" });

    if (device.user) {
      if (device.user.suspended)
        return response.status(400).json({ error: "User is suspended." });
      response.locals.user = device.user;
    }

    delete device.user;
    response.locals.device = device;
    next();
  } catch (error) {
    console.error("validDeviceToken", error);
    response.status(500).json({ error: "Invalid middleware response" });
  }
}


async function validRegistrationToken(request, response, next) {
  try {
    const authHeader = request.header("Authorization");
    const tempToken = authHeader ? authHeader.split(" ")[1] : null;
    if (!tempToken)
      return response
        .status(400)
        .json({ error: "Registration token is required" });

    const tempTokenData = MobileDevice.tempToken(tempToken);
    if (!tempTokenData)
      return response
        .status(400)
        .json({ error: "Invalid or expired registration token" });

    const multiUserMode = await SystemSettings.isMultiUserMode();
    if (multiUserMode) {
      if (!tempTokenData.userId)
        return response
          .status(400)
          .json({ error: "User id not found in registration token" });
      const user = await User.get({ id: Number(tempTokenData.userId) });
      if (!user) return response.status(400).json({ error: "User not found" });
      if (user.suspended)
        return response
          .status(400)
          .json({ error: "User is suspended - cannot register device" });
      response.locals.user = user;
    }

    next();
  } catch (error) {
    console.error("validRegistrationToken:error", error);
    response.status(500).json({
      error: "Invalid middleware response from validRegistrationToken",
    });
  }
}

module.exports = {
  validDeviceToken,
  validRegistrationToken,
};
