/**
 * Extracts text content from a multimodal message
 * If the content has multiple text items, it will join them together with a newline.
 * @param {string|Array} content - Message content that could be string or array of content objects
 * @returns {string} - The text content
 */
function extractTextContent(content) {
  if (!Array.isArray(content)) return content;
  return content
    .filter((item) => item.type === "text")
    .map((item) => item.text)
    .join("\n");
}


function getMimeTypeFromDataUrl(dataUrl) {
  try {
    const matches = dataUrl.match(/^data:([^;]+);base64,/);
    return matches ? matches[1].toLowerCase() : "image/png";
  } catch (e) {
    return "image/png";
  }
}


function extractAttachments(content) {
  if (!Array.isArray(content)) return [];
  return content
    .filter((item) => item.type === "image_url")
    .map((item, index) => ({
      name: `uploaded_image_${index}`,
      mime: getMimeTypeFromDataUrl(item.image_url.url),
      contentString: item.image_url.url,
    }));
}

module.exports = {
  extractTextContent,
  extractAttachments,
};
