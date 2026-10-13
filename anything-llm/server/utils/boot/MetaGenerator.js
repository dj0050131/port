


class MetaGenerator {
  name = "MetaGenerator";

  /** @type {MetaGenerator|null} */
  static _instance = null;

  /** @type {MetaTagDefinition[]|null} */
  #customConfig = null;

  #defaultManifest = {
    name: "AnythingLLM",
    short_name: "AnythingLLM",
    display: "standalone",
    orientation: "portrait",
    start_url: "/",
    icons: [
      {
        src: "/favicon.png",
        sizes: "any",
      },
    ],
  };

  constructor() {
    if (MetaGenerator._instance) return MetaGenerator._instance;
    MetaGenerator._instance = this;
  }

  #log(text, ...args) {
  }

  #defaultMeta() {
    return [
      {
        tag: "link",
        props: { type: "image/svg+xml", href: "/favicon.png" },
        content: null,
      },
      {
        tag: "title",
        props: null,
        content: "AnythingLLM | Your personal LLM trained on anything",
      },

      {
        tag: "meta",
        props: {
          name: "title",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },
      {
        tag: "meta",
        props: {
          description: "title",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },

      // <!-- Facebook -->
      { tag: "meta", props: { property: "og:type", content: "website" } },
      {
        tag: "meta",
        props: { property: "og:url", content: "https://anythingllm.com" },
      },
      {
        tag: "meta",
        props: {
          property: "og:title",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },
      {
        tag: "meta",
        props: {
          property: "og:description",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },
      {
        tag: "meta",
        props: {
          property: "og:image",
          content:
            "https://raw.githubusercontent.com/Mintplex-Labs/anything-llm/master/images/promo.png",
        },
      },

      // <!-- Twitter -->
      {
        tag: "meta",
        props: { property: "twitter:card", content: "summary_large_image" },
      },
      {
        tag: "meta",
        props: { property: "twitter:url", content: "https://anythingllm.com" },
      },
      {
        tag: "meta",
        props: {
          property: "twitter:title",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },
      {
        tag: "meta",
        props: {
          property: "twitter:description",
          content: "AnythingLLM | Your personal LLM trained on anything",
        },
      },
      {
        tag: "meta",
        props: {
          property: "twitter:image",
          content:
            "https://raw.githubusercontent.com/Mintplex-Labs/anything-llm/master/images/promo.png",
        },
      },

      { tag: "link", props: { rel: "icon", href: "/favicon.png" } },
      { tag: "link", props: { rel: "apple-touch-icon", href: "/favicon.png" } },

      // PWA specific tags
      {
        tag: "meta",
        props: { name: "mobile-web-app-capable", content: "yes" },
      },
      {
        tag: "meta",
        props: { name: "apple-mobile-web-app-capable", content: "yes" },
      },
      {
        tag: "meta",
        props: {
          name: "apple-mobile-web-app-status-bar-style",
          content: "black-translucent",
        },
      },
      { tag: "link", props: { rel: "manifest", href: "/manifest.json" } },
    ];
  }

  /**
   * Assembles Meta tags as one large string
   * @param {MetaTagDefinition[]} tagArray
   * @returns {string}
   */
  #assembleMeta() {
    const output = [];
    for (const tag of this.#customConfig) {
      let htmlString;
      htmlString = `<${tag.tag} `;

      if (tag.props !== null) {
        for (const [key, value] of Object.entries(tag.props))
          htmlString += `${key}="${value}" `;
      }

      if (tag.content) {
        htmlString += `>${tag.content}</${tag.tag}>`;
      } else {
        htmlString += `>`;
      }
      output.push(htmlString);
    }
    return output.join("\n");
  }

  #validUrl(faviconUrl = null) {
    if (faviconUrl === null) return "/favicon.png";
    try {
      const url = new URL(faviconUrl);
      return url.toString();
    } catch {
      return "/favicon.png";
    }
  }

  async #fetchConfg() {
    this.#log(`fetching custom meta tag settings...`);
    const { SystemSettings } = require("../../models/systemSettings");
    const customTitle = await SystemSettings.getValueOrFallback(
      { label: "meta_page_title" },
      null
    );
    const faviconURL = await SystemSettings.getValueOrFallback(
      { label: "meta_page_favicon" },
      null
    );

    if (customTitle === null && faviconURL === null) {
      this.#customConfig = this.#defaultMeta();
    } else {
      // When custom settings exist, include all default meta tags but override specific ones
      this.#customConfig = this.#defaultMeta().map((tag) => {
        if (tag.tag === "link" && tag.props?.rel === "icon") {
          return {
            tag: "link",
            props: { rel: "icon", href: this.#validUrl(faviconURL) },
          };
        }
        if (tag.tag === "title") {
          return {
            tag: "title",
            props: null,
            content:
              customTitle ??
              "AnythingLLM | Your personal LLM trained on anything",
          };
        }
        if (tag.tag === "meta" && tag.props?.name === "title") {
          return {
            tag: "meta",
            props: {
              name: "title",
              content:
                customTitle ??
                "AnythingLLM | Your personal LLM trained on anything",
            },
          };
        }
        if (tag.tag === "meta" && tag.props?.property === "og:title") {
          return {
            tag: "meta",
            props: {
              property: "og:title",
              content:
                customTitle ??
                "AnythingLLM | Your personal LLM trained on anything",
            },
          };
        }
        if (tag.tag === "meta" && tag.props?.property === "twitter:title") {
          return {
            tag: "meta",
            props: {
              property: "twitter:title",
              content:
                customTitle ??
                "AnythingLLM | Your personal LLM trained on anything",
            },
          };
        }
        // Override apple-touch-icon if custom favicon is set
        if (
          tag.tag === "link" &&
          tag.props?.rel === "apple-touch-icon" &&
          faviconURL
        ) {
          return {
            tag: "link",
            props: {
              rel: "apple-touch-icon",
              href: this.#validUrl(faviconURL),
            },
          };
        }
        return tag;
      });
    }

    return this.#customConfig;
  }

  /**
   * Clears the current config so it can be refetched on the server for next render.
   */
  clearConfig() {
    this.#customConfig = null;
  }

  
  async generate(response, code = 200) {
    if (this.#customConfig === null) await this.#fetchConfg();
    response.status(code).send(`
       <!DOCTYPE html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            ${this.#assembleMeta()}
            <script type="module" crossorigin src="/index.js"></script>
            <link rel="stylesheet" href="/index.css">
          </head>
          <body>
            <div id="root" class="h-screen"></div>
          </body>
        </html>`);
  }

  /**
   * Generates the manifest.json file for the PWA application on the fly.
   * @param {import('express').Response} response
   * @param {number} code
   */
  async generateManifest(response) {
    try {
      const { SystemSettings } = require("../../models/systemSettings");
      const manifestName = await SystemSettings.getValueOrFallback(
        { label: "meta_page_title" },
        "AnythingLLM"
      );
      const faviconURL = await SystemSettings.getValueOrFallback(
        { label: "meta_page_favicon" },
        null
      );

      let iconUrl = "/favicon.png";
      if (faviconURL) {
        try {
          new URL(faviconURL);
          iconUrl = faviconURL;
        } catch {
          iconUrl = "/favicon.png";
        }
      }

      const manifest = {
        name: manifestName,
        short_name: manifestName,
        display: "standalone",
        orientation: "portrait",
        start_url: "/",
        icons: [
          {
            src: iconUrl,
            sizes: "any",
          },
        ],
      };

      response.type("application/json").status(200).send(manifest).end();
    } catch (error) {
      this.#log(`error generating manifest: ${error.message}`, error);
      response
        .type("application/json")
        .status(200)
        .send(this.#defaultManifest)
        .end();
    }
  }
}

module.exports.MetaGenerator = MetaGenerator;
