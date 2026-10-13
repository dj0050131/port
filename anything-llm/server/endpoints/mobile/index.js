const { validatedRequest } = require("../../utils/middleware/validatedRequest");
const { MobileDevice } = require("../../models/mobileDevice");
const { handleMobileCommand } = require("./utils");
const { validDeviceToken, validRegistrationToken } = require("./middleware");
const { reqBody } = require("../../utils/http");
const {
  flexUserRoleValid,
  ROLES,
} = require("../../utils/middleware/multiUserProtected");

function mobileEndpoints(app) {
  if (!app) return;

  
  app.get(
    "/mobile/devices",
    [validatedRequest, flexUserRoleValid([ROLES.admin])],
    async (_request, response) => {
      try {
        const devices = await MobileDevice.where({}, null, null, {
          user: { select: { id: true, username: true } },
        });
        return response.status(200).json({ devices });
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );

  
  app.post(
    "/mobile/update/:id",
    [validatedRequest, flexUserRoleValid([ROLES.admin])],
    async (request, response) => {
      try {
        const body = reqBody(request);
        const updates = await MobileDevice.update(
          Number(request.params.id),
          body
        );
        if (updates.error)
          return response.status(400).json({ error: updates.error });
        return response.status(200).json({ updates });
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );

  
  app.delete(
    "/mobile/:id",
    [validatedRequest, flexUserRoleValid([ROLES.admin])],
    async (request, response) => {
      try {
        const device = await MobileDevice.get({
          id: Number(request.params.id),
        });
        if (!device)
          return response.status(404).json({ error: "Device not found" });
        await MobileDevice.delete(device.id);
        return response.status(200).json({ message: "Device deleted" });
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );

  app.get(
    "/mobile/connect-info",
    [validatedRequest, flexUserRoleValid([ROLES.admin])],
    async (_request, response) => {
      try {
        return response.status(200).json({
          connectionUrl: MobileDevice.connectionURL(response.locals?.user),
        });
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );

  /**
   * Checks if the device auth token is valid
   * against approved devices.
   */
  app.get("/mobile/auth", [validDeviceToken], async (_, response) => {
    try {
      return response
        .status(200)
        .json({ success: true, message: "Device authenticated" });
    } catch (e) {
      console.error(e);
      response.sendStatus(500).end();
    }
  });

  
  app.post(
    "/mobile/register",
    [validRegistrationToken],
    async (request, response) => {
      try {
        const body = reqBody(request);
        const result = await MobileDevice.create({
          deviceOs: body.deviceOs,
          deviceName: body.deviceName,
          userId: response.locals?.user?.id,
        });

        if (result.error)
          return response.status(400).json({ error: result.error });
        return response.status(200).json({
          token: result.device.token,
          platform: MobileDevice.platform,
        });
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );

  app.post(
    "/mobile/send/:command",
    [validDeviceToken],
    async (request, response) => {
      try {
        return handleMobileCommand(request, response);
      } catch (e) {
        console.error(e);
        response.sendStatus(500).end();
      }
    }
  );
}

module.exports = { mobileEndpoints };
