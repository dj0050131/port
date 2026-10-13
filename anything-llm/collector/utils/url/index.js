const RuntimeSettings = require("../runtimeSettings");


const VALID_PROTOCOLS = ["https:", "http:"];
const INVALID_OCTETS = [192, 172, 10, 127];
const runtimeSettings = new RuntimeSettings();


function isInvalidIp({ hostname }) {
  if (runtimeSettings.get("allowAnyIp")) {
    if (!runtimeSettings.get("seenAnyIpWarning")) {
      runtimeSettings.set("seenAnyIpWarning", true);
    }
    return false;
  }

  const IPRegex = new RegExp(
    /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/gi
  );

  // Not an IP address at all - passthrough
  if (!IPRegex.test(hostname)) return false;
  const [octetOne, ..._rest] = hostname.split(".");

  if (isNaN(Number(octetOne))) return true;

  // for locally hosted services or websites
  if (["127.0.0.1", "0.0.0.0"].includes(hostname)) return false;

  return INVALID_OCTETS.includes(Number(octetOne));
}


function validURL(url) {
  try {
    const destination = new URL(url);
    if (!VALID_PROTOCOLS.includes(destination.protocol)) return false;
    if (isInvalidIp(destination)) return false;
    return true;
  } catch {}
  return false;
}


function validateURL(url) {
  try {
    let destination = url.trim();
    // If the URL has a protocol, just pass through
    // If the URL doesn't have a protocol, assume https://
    if (destination.includes("://"))
      destination = new URL(destination).toString();
    else destination = new URL(`https://${destination}`).toString();

    // If the URL ends with a slash, remove it
    return destination.endsWith("/") ? destination.slice(0, -1) : destination;
  } catch {
    if (typeof url !== "string") return "";
    return url.trim();
  }
}


function validYoutubeVideoUrl(link, returnVideoId = false) {
  try {
    if (!link || typeof link !== "string") return false;
    let urlToValidate = link;

    if (!link.startsWith("http://") && !link.startsWith("https://")) {
      urlToValidate = "https://" + link;
      urlToValidate = new URL(urlToValidate).toString();
    }

    const regex =
      /^(?:https?:\/\/)?(?:www\.|m\.|music\.)?(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?(?:.*&)?v=|(?:live\/)?|shorts\/))([\w-]{11})(?:\S+)?$/;
    const match = urlToValidate.match(regex);
    if (returnVideoId) return match?.[1] ?? null;
    return !!match?.[1];
  } catch (error) {
    console.error("Error validating YouTube video URL", error);
    return returnVideoId ? null : false;
  }
}

module.exports = {
  validURL,
  validateURL,
  validYoutubeVideoUrl,
};
