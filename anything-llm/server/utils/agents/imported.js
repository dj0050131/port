const fs = require("fs");
const path = require("path");
const { safeJsonParse } = require("../http");
const { isWithin, normalizePath } = require("../files");
const { CollectorApi } = require("../collectorApi");
const pluginsPath =
  process.env.NODE_ENV === "development"
    ? path.resolve(__dirname, "../../storage/plugins/agent-skills")
    : path.resolve(process.env.STORAGE_DIR, "plugins", "agent-skills");
const sharedWebScraper = new CollectorApi();

class ImportedPlugin {
  constructor(config) {
    this.config = config;
    this.handlerLocation = path.resolve(
      pluginsPath,
      this.config.hubId,
      "handler.js"
    );
    delete require.cache[require.resolve(this.handlerLocation)];
    this.handler = require(this.handlerLocation);
    this.name = config.hubId;
    this.startupConfig = {
      params: {},
    };
  }

  /**
   * Gets the imported plugin handler.
   * @param {string} hubId - The hub ID of the plugin.
   * @returns {ImportedPlugin} - The plugin handler.
   */
  static loadPluginByHubId(hubId) {
    const configLocation = path.resolve(
      pluginsPath,
      normalizePath(hubId),
      "plugin.json"
    );
    if (!this.isValidLocation(configLocation)) return;
    const config = safeJsonParse(fs.readFileSync(configLocation, "utf8"));
    return new ImportedPlugin(config);
  }

  static isValidLocation(pathToValidate) {
    if (!isWithin(pluginsPath, pathToValidate)) return false;
    if (!fs.existsSync(pathToValidate)) return false;
    return true;
  }

  /**
   * Checks if the plugin folder exists and if it does not, creates the folder.
   */
  static checkPluginFolderExists() {
    const dir = path.resolve(pluginsPath);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    return;
  }

  /**
   * Loads plugins from `plugins` folder in storage that are custom loaded and defined.
   * only loads plugins that are active: true.
   * @returns {string[]} - array of plugin names to be loaded later.
   */
  static activeImportedPlugins() {
    const plugins = [];
    this.checkPluginFolderExists();
    const folders = fs.readdirSync(path.resolve(pluginsPath));
    for (const folder of folders) {
      const configLocation = path.resolve(
        pluginsPath,
        normalizePath(folder),
        "plugin.json"
      );
      if (!this.isValidLocation(configLocation)) continue;
      const config = safeJsonParse(fs.readFileSync(configLocation, "utf8"));
      if (config.active) plugins.push(`@@${config.hubId}`);
    }
    return plugins;
  }

  /**
   * Lists all imported plugins.
   * @returns {Array} - array of plugin configurations (JSON).
   */
  static listImportedPlugins() {
    const plugins = [];
    this.checkPluginFolderExists();
    if (!fs.existsSync(pluginsPath)) return plugins;

    const folders = fs.readdirSync(path.resolve(pluginsPath));
    for (const folder of folders) {
      const configLocation = path.resolve(
        pluginsPath,
        normalizePath(folder),
        "plugin.json"
      );
      if (!this.isValidLocation(configLocation)) continue;
      const config = safeJsonParse(fs.readFileSync(configLocation, "utf8"));
      plugins.push(config);
    }
    return plugins;
  }

  
  static updateImportedPlugin(hubId, config) {
    const configLocation = path.resolve(
      pluginsPath,
      normalizePath(hubId),
      "plugin.json"
    );
    if (!this.isValidLocation(configLocation)) return;

    const currentConfig = safeJsonParse(
      fs.readFileSync(configLocation, "utf8"),
      null
    );
    if (!currentConfig) return;

    const updatedConfig = { ...currentConfig, ...config };
    fs.writeFileSync(configLocation, JSON.stringify(updatedConfig, null, 2));
    return updatedConfig;
  }

  /**
   * Deletes a plugin. Removes the entire folder of the object.
   * @param {string} hubId - The hub ID of the plugin.
   * @returns {boolean} - True if the plugin was deleted, false otherwise.
   */
  static deletePlugin(hubId) {
    if (!hubId) throw new Error("No plugin hubID passed.");
    const pluginFolder = path.resolve(pluginsPath, normalizePath(hubId));
    if (!this.isValidLocation(pluginFolder)) return;
    fs.rmSync(pluginFolder, { recursive: true });
    return true;
  }

  /**
  /**
   * Validates if the handler.js file exists for the given plugin.
   * @param {string} hubId - The hub ID of the plugin.
   * @returns {boolean} - True if the handler.js file exists, false otherwise.
   */
  static validateImportedPluginHandler(hubId) {
    const handlerLocation = path.resolve(
      pluginsPath,
      normalizePath(hubId),
      "handler.js"
    );
    return this.isValidLocation(handlerLocation);
  }

  parseCallOptions() {
    const callOpts = {};
    if (!this.config.setup_args || typeof this.config.setup_args !== "object") {
      return callOpts;
    }
    for (const [param, definition] of Object.entries(this.config.setup_args)) {
      if (definition.required && !definition?.value) {
        continue;
      }
      callOpts[param] = definition.value || definition.default || null;
    }
    return callOpts;
  }

  plugin(runtimeArgs = {}) {
    const customFunctions = this.handler.runtime;
    return {
      runtimeArgs,
      name: this.name,
      config: this.config,
      setup(aibitat) {
        aibitat.function({
          super: aibitat,
          name: this.name,
          config: this.config,
          runtimeArgs: this.runtimeArgs,
          description: this.config.description,
          logger: aibitat?.handlerProps?.log || console.log, // Allows plugin to log to the console.
          introspect: aibitat?.introspect || console.log, // Allows plugin to display a "thought" the chat window UI.
          runtime: "docker",
          webScraper: sharedWebScraper,
          examples: this.config.examples ?? [],
          parameters: {
            $schema: "http://json-schema.org/draft-07/schema#",
            type: "object",
            properties: this.config.entrypoint.params ?? {},
            additionalProperties: false,
          },
          ...customFunctions,
        });
      },
    };
  }

  
  static async importCommunityItemFromUrl(url, item) {
    this.checkPluginFolderExists();
    const hubId = item.id;
    if (!hubId) return { success: false, error: "No hubId passed to import." };

    const zipFilePath = path.resolve(pluginsPath, `${item.id}.zip`);
    const pluginFile = item.manifest.files.find(
      (file) => file.name === "plugin.json"
    );
    if (!pluginFile)
      return {
        success: false,
        error: "No plugin.json file found in manifest.",
      };

    const pluginFolder = path.resolve(pluginsPath, normalizePath(hubId));
    if (fs.existsSync(pluginFolder))

    try {
      const protocol = new URL(url).protocol.replace(":", "");
      const httpLib = protocol === "https" ? require("https") : require("http");

      const downloadZipFile = new Promise(async (resolve) => {
        try {.origin
          );
          const zipFile = fs.createWriteStream(zipFilePath);
          const request = httpLib.get(url, function (response) {
            response.pipe(zipFile);
            zipFile.on("finish", () => {
              resolve(true);
            });
          });

          request.on("error", (error) => {
            console.error(
              "ImportedPlugin.importCommunityItemFromUrl - error downloading zip file: ",
              error
            );
            resolve(false);
          });
        } catch (error) {
          console.error(
            "ImportedPlugin.importCommunityItemFromUrl - error downloading zip file: ",
            error
          );
          resolve(false);
        }
      });

      const success = await downloadZipFile;
      if (!success)
        return { success: false, error: "Failed to download zip file." };

      // Unzip the file to the plugin folder
      // Note: https://github.com/cthackers/adm-zip?tab=readme-ov-file#electron-original-fs
      const AdmZip = require("adm-zip");
      const zip = new AdmZip(zipFilePath);
      zip.extractAllTo(pluginFolder);

      // We want to make sure specific keys are set to the proper values for
      // plugin.json so we read and overwrite the file with the proper values.
      const pluginJsonPath = path.resolve(pluginFolder, "plugin.json");
      const pluginJson = safeJsonParse(fs.readFileSync(pluginJsonPath, "utf8"));
      pluginJson.active = false;
      pluginJson.hubId = hubId;
      fs.writeFileSync(pluginJsonPath, JSON.stringify(pluginJson, null, 2));
      return { success: true, error: null };
    } catch (error) {
      console.error(
        "ImportedPlugin.importCommunityItemFromUrl - error: ",
        error
      );
      return { success: false, error: error.message };
    } finally {
      if (fs.existsSync(zipFilePath)) fs.unlinkSync(zipFilePath);
    }
  }
}

module.exports = ImportedPlugin;
