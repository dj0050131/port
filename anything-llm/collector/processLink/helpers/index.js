const path = require("path");
const { validURL } = require("../../utils/url");
const { processSingleFile } = require("../../processSingleFile");
const { downloadURIToFile } = require("../../utils/downloadURIToFile");
const { ACCEPTED_MIMES } = require("../../utils/constants");
const { validYoutubeVideoUrl } = require("../../utils/url");


async function getContentTypeFromURL(url) {
  try {
    if (!url || typeof url !== "string" || !validURL(url))
      return { success: false, reason: "Not a valid URL.", contentType: null };

    const abortController = new AbortController();
    const timeout = setTimeout(() => {
      abortController.abort();
      console.error("Timeout fetching content type for URL:", url.toString());
    }, 5_000);

    const res = await fetch(url, {
      method: "HEAD",
      signal: abortController.signal,
    }).finally(() => clearTimeout(timeout));

    if (!res.ok)
      return {
        success: false,
        reason: `HTTP ${res.status}: ${res.statusText}`,
        contentType: null,
      };

    const contentType = res.headers.get("Content-Type")?.toLowerCase();
    const contentTypeWithoutCharset = contentType?.split(";")[0].trim();
    if (!contentTypeWithoutCharset)
      return {
        success: false,
        reason: "No Content-Type found.",
        contentType: null,
      };
    return {
      success: true,
      reason: null,
      contentType: contentTypeWithoutCharset,
    };
  } catch (error) {
    return {
      success: false,
      reason: `Error: ${error.message}`,
      contentType: null,
    };
  }
}


function returnResult({
  success,
  reason,
  documents,
  content,
  saveAsDocument = true,
} = {}) {
  if (!saveAsDocument) {
    return {
      success,
      content,
    };
  } else return { success, reason, documents };
}


async function determineContentType(uri) {
  let processVia = "web";

  // Dont check for content type if it is a YouTube video URL
  if (validYoutubeVideoUrl(uri))
    return { contentType: "text/html", processVia: "youtube" };

  return await getContentTypeFromURL(uri)
    .then((result) => {
      if (!!result.reason) console.error(result.reason);

      // If the content type is not text/html or text/plain, and it is in the ACCEPTED_MIMES,
      // then we can process it as a file
      if (
        !!result.contentType &&
        !["text/html", "text/plain"].includes(result.contentType) &&
        result.contentType in ACCEPTED_MIMES
      )
        processVia = "file";

      return { contentType: result.contentType, processVia };
    })
    .catch((error) => {
      console.error("Error getting content type from URL", error);
      return { contentType: null, processVia };
    });
}


async function processAsFile({ uri, saveAsDocument = true }) {
  const fileContentResult = await downloadURIToFile(uri);
  if (!fileContentResult.success)
    return returnResult({
      success: false,
      reason: fileContentResult.reason,
      documents: [],
      content: null,
      saveAsDocument,
    });

  const fileFilePath = fileContentResult.fileLocation;
  const targetFilename = path.basename(fileFilePath);

  /**
   * If the saveAsDocument is false, we are only interested in the text content
   * and can ignore the file as a document by using `parseOnly` in the options.
   * This will send the file to the Direct Uploads folder instead of the Documents folder.
   * that will be deleted by the cleanup-orphan-documents job that runs frequently. The trade off
   * is that since it still is in FS we can debug its output or even potentially reuse it for other purposes.
   *
   * TODO: Improve this process via a new option that will instantly delete the file after processing
   * if we find we dont need this file ever after processing.
   */
  const processSingleFileResult = await processSingleFile(targetFilename, {
    parseOnly: saveAsDocument === false,
  });
  if (!processSingleFileResult.success) {
    return returnResult({
      success: false,
      reason: processSingleFileResult.reason,
      documents: [],
      content: null,
      saveAsDocument,
    });
  }

  // If we intend to return only the text content, return the content from the file
  if (!saveAsDocument) {
    return returnResult({
      success: true,
      content: processSingleFileResult.documents[0].pageContent,
      saveAsDocument,
    });
  }

  return processSingleFileResult;
}

module.exports = {
  returnResult,
  getContentTypeFromURL,
  determineContentType,
  processAsFile,
};
