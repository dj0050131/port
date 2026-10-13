const { getGitVersion } = require("../../endpoints/utils");
const { Telemetry } = require("../../models/telemetry");

function checkColumnTemplate(tablename = null, column = null) {
  if (!tablename || !column)
    throw new Error(`Migration Error`, { tablename, column });
  return `SELECT COUNT(*) AS _exists FROM pragma_table_info('${tablename}') WHERE name='${column}'`;
}

// Note (tcarambat): Since there is no good way to track migrations in Node/SQLite we use this simple system
// { colName: 'stringColName', execCmd: `SQL Command to run when`, doif: boolean },
// doif: true

async function checkForMigrations(model, db) {
  if (model.migrations().length === 0) return;
  const toMigrate = [];
  for (const { colName, execCmd, doif } of model.migrations()) {
    const { _exists } = await db.get(
      checkColumnTemplate(model.tablename, colName)
    );
    const colExists = _exists !== 0;
    if (colExists !== doif) continue;

    toMigrate.push(execCmd);
  }

  if (toMigrate.length === 0) return;
  await db.exec(toMigrate.join(";\n"));
  return;
}

// Note(tcarambat): When building in production via Docker the SQLite file will not exist
async function validateTablePragmas(force = false) {
  try {
    if (process.env.NODE_ENV !== "development" && force === false) {
      return;
    }
    const { SystemSettings } = require("../../models/systemSettings");
    const { User } = require("../../models/user");
    const { Workspace } = require("../../models/workspace");
    const { WorkspaceUser } = require("../../models/workspaceUsers");
    const { Document } = require("../../models/documents");
    const { DocumentVectors } = require("../../models/vectors");
    const { WorkspaceChats } = require("../../models/workspaceChats");
    const { Invite } = require("../../models/invite");
    const { WelcomeMessages } = require("../../models/welcomeMessages");
    const { ApiKey } = require("../../models/apiKeys");

    await SystemSettings.migrateTable();
    await User.migrateTable();
    await Workspace.migrateTable();
    await WorkspaceUser.migrateTable();
    await Document.migrateTable();
    await DocumentVectors.migrateTable();
    await WorkspaceChats.migrateTable();
    await Invite.migrateTable();
    await WelcomeMessages.migrateTable();
    await ApiKey.migrateTable();
  } catch (e) {
    console.error(`validateTablePragmas: Migrations failed`, e);
  }
  return;
}

async function setupTelemetry() {
  if (process.env.DISABLE_TELEMETRY === "true") {
    return true;
  }

  if (Telemetry.isDev()) {
    return;
  }
  await Telemetry.findOrCreateId();
  await Telemetry.sendTelemetry("server_boot", {
    commit: getGitVersion(),
  });
  return;
}

module.exports = {
  checkForMigrations,
  validateTablePragmas,
  setupTelemetry,
};
