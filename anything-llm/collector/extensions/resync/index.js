const { getLinkText } = require("../../processLink");


async function resyncLink({ link }, response) {
  if (!link) throw new Error('Invalid link provided');
  try {
    const { success, content = null } = await getLinkText(link);
    if (!success) throw new Error(`Failed to sync link content. ${reason}`);
    response.status(200).json({ success, content });
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}


async function resyncYouTube({ link }, response) {
  if (!link) throw new Error('Invalid link provided');
  try {
    const { fetchVideoTranscriptContent } = require("../../utils/extensions/YoutubeTranscript");
    const { success, reason, content } = await fetchVideoTranscriptContent({ url: link });
    if (!success) throw new Error(`Failed to sync YouTube video transcript. ${reason}`);
    response.status(200).json({ success, content });
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}


async function resyncConfluence({ chunkSource }, response) {
  if (!chunkSource) throw new Error('Invalid source property provided');
  try {
    const source = response.locals.encryptionWorker.expandPayload(chunkSource);
    const { fetchConfluencePage } = require("../../utils/extensions/Confluence");
    const { success, reason, content } = await fetchConfluencePage({
      pageUrl: `https:${source.pathname}`, // need to add back the real protocol
      baseUrl: source.searchParams.get('baseUrl'),
      spaceKey: source.searchParams.get('spaceKey'),
      accessToken: source.searchParams.get('token'),
      username: source.searchParams.get('username'),
      cloud: source.searchParams.get('cloud') === 'true',
      bypassSSL: source.searchParams.get('bypassSSL') === 'true',
    });

    if (!success) throw new Error(`Failed to sync Confluence page content. ${reason}`);
    response.status(200).json({ success, content });
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}


async function resyncGithub({ chunkSource }, response) {
  if (!chunkSource) throw new Error('Invalid source property provided');
  try {
    const source = response.locals.encryptionWorker.expandPayload(chunkSource);
    const { fetchGithubFile } = require("../../utils/extensions/RepoLoader/GithubRepo");
    const { success, reason, content } = await fetchGithubFile({
      repoUrl: `https:${source.pathname}`, // need to add back the real protocol
      branch: source.searchParams.get('branch'),
      accessToken: source.searchParams.get('pat'),
      sourceFilePath: source.searchParams.get('path'),
    });

    if (!success) throw new Error(`Failed to sync GitHub file content. ${reason}`);
    response.status(200).json({ success, content });
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}



async function resyncDrupalWiki({ chunkSource }, response) {
  if (!chunkSource) throw new Error('Invalid source property provided');
  try {
    const source = response.locals.encryptionWorker.expandPayload(chunkSource);
    const { loadPage } = require("../../utils/extensions/DrupalWiki");
    const { success, reason, content } = await loadPage({
      baseUrl: source.searchParams.get('baseUrl'),
      pageId: source.searchParams.get('pageId'),
      accessToken: source.searchParams.get('accessToken'),
    });

    if (!success) {
      console.error(`Failed to sync DrupalWiki page content. ${reason}`);
      response.status(200).json({
        success: false,
        content: null,
      });
    } else {
      response.status(200).json({ success, content });
    }
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}


async function resyncPaperlessNgx({ chunkSource }, response) {
  if (!chunkSource) throw new Error('Invalid source property provided');
  try {
    const source = response.locals.encryptionWorker.expandPayload(chunkSource);
    const { PaperlessNgxLoader } = require("../../utils/extensions/PaperlessNgx/PaperlessNgxLoader");
    const loader = new PaperlessNgxLoader({
      baseUrl: source.searchParams.get('baseUrl'),
      apiToken: source.searchParams.get('token'),
    });
    const documentId = source.pathname.split('//')[1];
    const content = await loader.fetchDocumentContent(documentId);

    if (!content) throw new Error('Failed to fetch document content');
    response.status(200).json({ success: true, content });
  } catch (e) {
    console.error(e);
    response.status(200).json({
      success: false,
      content: null,
    });
  }
}

module.exports = {
  link: resyncLink,
  youtube: resyncYouTube,
  confluence: resyncConfluence,
  github: resyncGithub,
  drupalwiki: resyncDrupalWiki,
  "paperless-ngx": resyncPaperlessNgx,
}
