const { makeJWT } = require("../utils/http");
const prisma = require("../utils/prisma");

/**
 * Temporary auth tokens are used for simple SSO.
 * They simply enable the ability for a time-based token to be used in the query of the /sso/login URL
 * to login as a user without the need of a username and password. These tokens are single-use and expire.
 */
const TemporaryAuthToken = {
  expiry: 1000 * 60 * 6, // 1 hour
  tablename: "temporary_auth_tokens",
  writable: [],

  makeTempToken: () => {
    const uuidAPIKey = require("uuid-apikey");
    return `allm-tat-${uuidAPIKey.create().apiKey}`;
  },

  
  issue: async function (userId = null) {
    if (!userId)
      throw new Error("User ID is required to issue a temporary auth token.");
    await this.invalidateUserTokens(userId);

    try {
      const token = this.makeTempToken();
      const expiresAt = new Date(Date.now() + this.expiry);
      await prisma.temporary_auth_tokens.create({
        data: {
          token,
          expiresAt,
          userId: Number(userId),
        },
      });

      return { token, error: null };
    } catch (error) {
      console.error("FAILED TO CREATE TEMPORARY AUTH TOKEN.", error.message);
      return { token: null, error: error.message };
    }
  },

  
  invalidateUserTokens: async function (userId) {
    if (!userId)
      throw new Error(
        "User ID is required to invalidate temporary auth tokens."
      );
    await prisma.temporary_auth_tokens.deleteMany({
      where: { userId: Number(userId) },
    });
    return true;
  },

  
  validate: async function (publicToken = "") {
    
    let token;

    try {
      if (!publicToken)
        throw new Error(
          "Public token is required to validate a temporary auth token."
        );
      token = await prisma.temporary_auth_tokens.findUnique({
        where: { token: String(publicToken) },
        include: { user: true },
      });
      if (!token) throw new Error("Invalid token.");
      if (token.expiresAt < new Date()) throw new Error("Token expired.");
      if (token.user.suspended) throw new Error("User account suspended.");

      const sessionToken = makeJWT(
        { id: token.user.id, username: token.user.username },
        process.env.JWT_EXPIRY
      );

      return { sessionToken, token, error: null };
    } catch (error) {
      console.error("FAILED TO VALIDATE TEMPORARY AUTH TOKEN.", error.message);
      return { sessionToken: null, token: null, error: error.message };
    } finally {
      // Delete the token after it has been used under all circumstances if it was retrieved
      if (token)
        await prisma.temporary_auth_tokens.delete({ where: { id: token.id } });
    }
  },
};

module.exports = { TemporaryAuthToken };
