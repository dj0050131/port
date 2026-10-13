

const { htmlToText } = require("html-to-text");
const { tokenizeString } = require("../../../tokenizer");
const {
  sanitizeFileName,
  writeToServerDocuments,
  documentsFolder,
  normalizePath,
  isWithin,
} = require("../../../files");
const { default: slugify } = require("slugify");
const path = require("path");
const fs = require("fs");
const { processSingleFile } = require("../../../../processSingleFile");
const {
  WATCH_DIRECTORY,
  SUPPORTED_FILETYPE_CONVERTERS,
} = require("../../../constants");

class Page {
  
  constructor({ id, title, created, type, processedBody, url, spaceId }) {
    this.id = id;
    this.title = title;
    this.url = url;
    this.created = created;
    this.type = type;
    this.processedBody = processedBody;
    this.spaceId = spaceId;
  }
}

class DrupalWiki {
  /**
   *
   * @param baseUrl
   * @param spaceId
   * @param accessToken
   */
  constructor({ baseUrl, accessToken }) {
    this.baseUrl = baseUrl;
    this.accessToken = accessToken;
    this.storagePath = this.#prepareStoragePath(baseUrl);
  }

  
  async loadAndStoreAllPagesForSpace(spaceId, encryptionWorker) {
    const pageIndex = await this.#getPageIndexForSpace(spaceId);
    for (const pageId of pageIndex) {
      try {
        const page = await this.loadPage(pageId);

        // Pages with an empty body will lead to embedding issues / exceptions
        if (page.processedBody.trim() !== "") {
          this.#storePage(page, encryptionWorker);
          await this.#downloadAndProcessAttachments(page.id);
        } else { since it has no content`);
        }
      } catch (e) {
        console.error(
          `Could not process DrupalWiki page ${pageId} (skipping and continuing): `
        );
        console.error(e);
      }
    }
  }

  
  async loadPage(pageId) {
    return this.#fetchPage(pageId);
  }

  
  async #getPageIndexForSpace(spaceId) {
    let hasNext = true;
    let pageIds = [];
    let pageNr = 0;
    do {
      let { isLast, pageIdsForPage } = await this.#getPagesForSpacePaginated(
        spaceId,
        pageNr
      );
      hasNext = !isLast;
      pageNr++;
      if (pageIdsForPage.length) {
        pageIds = pageIds.concat(pageIdsForPage);
      }
    } while (hasNext);

    return pageIds;
  }

  
  async #getPagesForSpacePaginated(spaceId, pageNr) {
    /*
     * {
     *   content: Page[],
     *   last: boolean,
     *   pageable: {
     *     pageNumber: number
     *   }
     * }
     */
    const data = await this._doFetch(
      `${this.baseUrl}/api/rest/scope/api/page?size=100&space=${spaceId}&page=${pageNr}`
    );

    const pageIds = data.content.map((page) => {
      return Number(page.id);
    });

    return {
      isLast: data.last,
      pageIdsForPage: pageIds,
    };
  }

  
  async #fetchPage(pageId) {
    const data = await this._doFetch(
      `${this.baseUrl}/api/rest/scope/api/page/${pageId}`
    );
    const url = `${this.baseUrl}/node/${data.id}`;
    return new Page({
      id: data.id,
      title: data.title,
      created: data.lastModified,
      type: data.type,
      processedBody: this.#processPageBody({
        body: data.body,
        title: data.title,
        lastModified: data.lastModified,
        url: url,
      }),
      url: url,
    });
  }

  
  #storePage(page, encryptionWorker) {
    const { hostname } = new URL(this.baseUrl);

    // This UUID will ensure that re-importing the same page without any changes will not
    const targetUUID = `${hostname}.${page.spaceId}.${page.id}.${page.created}`;
    const wordCount = page.processedBody.split(" ").length;
    const data = {
      id: targetUUID,
      url: `drupalwiki://${page.url}`,
      title: page.title,
      docAuthor: this.baseUrl,
      description: page.title,
      docSource: `${this.baseUrl} DrupalWiki`,
      chunkSource: this.#generateChunkSource(page.id, encryptionWorker),
      published: new Date().toLocaleString(),
      wordCount: wordCount,
      pageContent: page.processedBody,
      token_count_estimate: tokenizeString(page.processedBody),
    };

    const fileName = sanitizeFileName(`${slugify(page.title)}-${data.id}`); to '${this.storagePath}/${fileName}'`
    );
    writeToServerDocuments({
      data,
      filename: fileName,
      destinationOverride: this.storagePath,
    });
  }

  
  #generateChunkSource(pageId, encryptionWorker) {
    const payload = {
      baseUrl: this.baseUrl,
      pageId: pageId,
      accessToken: this.accessToken,
    };
    return `drupalwiki://${
      this.baseUrl
    }/node/${pageId}?payload=${encryptionWorker.encrypt(
      JSON.stringify(payload)
    )}`;
  }

  async _doFetch(url) {
    const response = await fetch(url, {
      headers: this.#getHeaders(),
    });
    if (!response.ok) {
      throw new Error(`Failed to fetch ${url}: ${response.status}`);
    }
    return response.json();
  }

  #getHeaders() {
    return {
      "Content-Type": "application/json",
      Accept: "application/json",
      Authorization: `Bearer ${this.accessToken}`,
    };
  }

  #prepareStoragePath(baseUrl) {
    const { hostname } = new URL(baseUrl);
    const subFolder = slugify(`drupalwiki-${hostname}`).toLowerCase();
    const outFolder = path.resolve(documentsFolder, subFolder);
    if (!fs.existsSync(outFolder)) fs.mkdirSync(outFolder, { recursive: true });
    return outFolder;
  }

  
  #processPageBody({ body, url, title, lastModified }) {
    const textContent = body.trim() !== "" ? body : title;

    const plainTextContent = htmlToText(textContent, {
      wordwrap: false,
      preserveNewlines: true,
      selectors: [
        {
          selector: "table",
          format: "dataTable",
          options: {
            colSpacing: 3,
            rowSpacing: 1,
            uppercaseHeaderCells: true,
            maxColumnWidth: Infinity,
          },
        },
      ],
    });

    const plainBody = plainTextContent.replace(/\n{3,}/g, "\n\n");
    return plainBody;
  }

  async #downloadAndProcessAttachments(pageId) {
    try {
      const data = await this._doFetch(
        `${this.baseUrl}/api/rest/scope/api/attachment?pageId=${pageId}&size=2000`
      );

      const extensionsList = Object.keys(SUPPORTED_FILETYPE_CONVERTERS);
      for (const attachment of data.content || data) {
        const { fileName, id: attachId } = attachment;
        const lowerName = fileName.toLowerCase();
        if (!extensionsList.some((ext) => lowerName.endsWith(ext))) {
          continue;
        }

        const downloadUrl = `${this.baseUrl}/api/rest/scope/api/attachment/${attachId}/download`;
        const attachmentResponse = await fetch(downloadUrl, {
          headers: this.#getHeaders(),
        });
        if (!attachmentResponse.ok) {
          continue;
        }

        const buffer = await attachmentResponse.arrayBuffer();
        const localFilePath = normalizePath(
          sanitizeFileName(path.resolve(WATCH_DIRECTORY, fileName))
        );
        if (!isWithin(path.resolve(WATCH_DIRECTORY), localFilePath)) {
          console.error(
            `[DrupalWiki Loader]: File name ${localFilePath} is not within the storage path ${path.resolve(
              WATCH_DIRECTORY
            )}`
          );
          continue;
        }

        require("fs").writeFileSync(localFilePath, Buffer.from(buffer));
        await processSingleFile(localFilePath);
      }
    } catch (err) {
      console.error(`Fetching/processing attachments failed:`, err);
    }
  }
}

module.exports = { DrupalWiki };
