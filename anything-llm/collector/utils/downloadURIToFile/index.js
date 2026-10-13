const { WATCH_DIRECTORY } = require("../constants");
const fs = require("fs");
const path = require("path");
const { pipeline } = require("stream/promises");
const { validURL } = require("../url");
const { default: slugify } = require("slugify");


async function downloadURIToFile(url, maxTimeout = 10_000) {
  if (!url || typeof url !== "string" || !validURL(url))
    return { success: false, reason: "Not a valid URL.", fileLocation: null };

  try {
    const abortController = new AbortController();
    const timeout = setTimeout(() => {
      abortController.abort();
      console.error(
        `Timeout ${maxTimeout}ms reached while downloading file for URL:`,
        url.toString()
      );
    }, maxTimeout);

    const res = await fetch(url, { signal: abortController.signal })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        return res;
      })
      .finally(() => clearTimeout(timeout));

    const urlObj = new URL(url);
    const filename = `${urlObj.hostname}-${slugify(
      urlObj.pathname.replace(/\//g, "-"),
      { lower: true }
    )}`;
    const localFilePath = path.join(WATCH_DIRECTORY, filename);
    const writeStream = fs.createWriteStream(localFilePath);
    await pipeline(res.body, writeStream);
    return { success: true, fileLocation: localFilePath, reason: null };
  } catch (error) {
    console.error(`Error writing to hotdir: ${error} for URL: ${url}`);
    return { success: false, reason: error.message, fileLocation: null };
  }
}

module.exports = {
  downloadURIToFile,
};
