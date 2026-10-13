const { BackgroundService } = require("../utils/BackgroundWorkers");
const prisma = require("../utils/prisma");
const { SystemSettings } = require("./systemSettings");
const { Telemetry } = require("./telemetry");



const DocumentSyncQueue = {
  featureKey: "experimental_live_file_sync",
  validFileTypes: [
    "link",
    "youtube",
    "confluence",
    "github",
    "gitlab",
    "drupalwiki",
  ],
  defaultStaleAfter: 604800000,
  maxRepeatFailures: 5, // How many times a run can fail in a row before pruning.
  writable: [],

  bootWorkers: function () {
    new BackgroundService().boot();
  },

  killWorkers: function () {
    new BackgroundService().stop();
  },

  /** Check is the Document Sync/Watch feature is enabled and can be used. */
  enabled: async function () {
    return (
      (await SystemSettings.get({ label: this.featureKey }))?.value ===
      "enabled"
    );
  },

  
  calcNextSync: function (queueRecord) {
    return new Date(Number(new Date()) + queueRecord.staleAfterMs);
  },

  
  canWatch: function ({ title, chunkSource = null } = {}) {
    if (!chunkSource) return false;

    if (chunkSource.startsWith("link://") && title.endsWith(".html"))
      return true; // If is web-link material (prior to feature most chunkSources were links://)
    if (chunkSource.startsWith("youtube://")) return true; // If is a youtube link
    if (chunkSource.startsWith("confluence://")) return true; // If is a confluence document link
    if (chunkSource.startsWith("github://")) return true; // If is a GitHub file reference
    if (chunkSource.startsWith("gitlab://")) return true; // If is a GitLab file reference
    if (chunkSource.startsWith("drupalwiki://")) return true; // If is a DrupalWiki document link
    return false;
  },

  
  watch: async function (document = null) {
    if (!document) return false;
    try {
      const { Document } = require("./documents");

      // non-zero then we exit early so that we do not have duplicated watch queues for the same file
      const workspaceDocIds = (
        await Document.where({ filename: document.filename, watched: true })
      ).map((rec) => rec.id);
      const hasRecords =
        (await this.count({ workspaceDocId: { in: workspaceDocIds } })) > 0;
      if (hasRecords)
        throw new Error(
          `Cannot watch this document again - it already has a queue set.`
        );

      const queue = await prisma.document_sync_queues.create({
        data: {
          workspaceDocId: document.id,
          nextSyncAt: new Date(Number(new Date()) + this.defaultStaleAfter),
        },
      });
      await Document._updateAll(
        { filename: document.filename },
        { watched: true }
      );
      return queue || null;
    } catch (error) {
      console.error(error.message);
      return null;
    }
  },

  
  unwatch: async function (document = null) {
    if (!document) return false;
    try {
      const { Document } = require("./documents");

      const workspaceDocIds = (
        await Document.where({ filename: document.filename, watched: true })
      ).map((rec) => rec.id);
      await this.delete({ workspaceDocId: { in: workspaceDocIds } });
      await Document._updateAll(
        { filename: document.filename },
        { watched: false }
      );
      return true;
    } catch (error) {
      console.error(error.message);
      return false;
    }
  },

  _update: async function (id = null, data = {}) {
    if (!id) throw new Error("No id provided for update");

    try {
      await prisma.document_sync_queues.update({
        where: { id },
        data,
      });
      return true;
    } catch (error) {
      console.error(error.message);
      return false;
    }
  },

  get: async function (clause = {}) {
    try {
      const queue = await prisma.document_sync_queues.findFirst({
        where: clause,
      });
      return queue || null;
    } catch (error) {
      console.error(error.message);
      return null;
    }
  },

  where: async function (
    clause = {},
    limit = null,
    orderBy = null,
    include = {}
  ) {
    try {
      const results = await prisma.document_sync_queues.findMany({
        where: clause,
        ...(limit !== null ? { take: limit } : {}),
        ...(orderBy !== null ? { orderBy } : {}),
        ...(include !== null ? { include } : {}),
      });
      return results;
    } catch (error) {
      console.error(error.message);
      return [];
    }
  },

  count: async function (clause = {}, limit = null) {
    try {
      const count = await prisma.document_sync_queues.count({
        where: clause,
        ...(limit !== null ? { take: limit } : {}),
      });
      return count;
    } catch (error) {
      console.error("FAILED TO COUNT DOCUMENTS.", error.message);
      return 0;
    }
  },

  delete: async function (clause = {}) {
    try {
      await prisma.document_sync_queues.deleteMany({ where: clause });
      return true;
    } catch (error) {
      console.error(error.message);
      return false;
    }
  },

  
  staleDocumentQueues: async function () {
    const queues = await this.where(
      {
        nextSyncAt: {
          lte: new Date().toISOString(),
        },
      },
      null,
      null,
      {
        workspaceDoc: {
          include: {
            workspace: true,
          },
        },
      }
    );
    return queues;
  },

  saveRun: async function (queueId = null, status = null, result = {}) {
    const { DocumentSyncRun } = require("./documentSyncRun");
    return DocumentSyncRun.save(queueId, status, result);
  },

  
  toggleWatchStatus: async function (documentRecord, watchStatus = false) {
    if (!watchStatus) {
      await Telemetry.sendTelemetry("document_unwatched");
      await this.unwatch(documentRecord);
      return;
    }

    await this.watch(documentRecord);
    await Telemetry.sendTelemetry("document_watched");
    return;
  },
};

module.exports = { DocumentSyncQueue };
