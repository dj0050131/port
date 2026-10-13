

/**
 * @class
 * @classdesc Loads and manages GitHub repository content.
 */
class GitHubRepoLoader {
  /**
   * Creates an instance of RepoLoader.
   * @param {RepoLoaderArgs} [args] - The configuration options.
   * @returns {GitHubRepoLoader}
   */
  constructor(args = {}) {
    this.ready = false;
    this.repo = this.#processRepoUrl(args?.repo);
    this.branch = args?.branch;
    this.accessToken = args?.accessToken || null;
    this.ignorePaths = args?.ignorePaths || [];

    this.author = null;
    this.project = null;
    this.branches = [];
  }

  /**
   * Processes a repository URL to ensure it is in the correct format
   * - remove the .git suffix if present
   * - ensure the url is valid
   * @param {string} repoUrl - The repository URL to process.
   * @returns {string|null} The processed repository URL, or null if the URL is invalid.
   */
  #processRepoUrl(repoUrl) {
    if (!repoUrl) return repoUrl;
    try {
      const url = new URL(repoUrl);
      if (url.pathname.endsWith(".git"))
        url.pathname = url.pathname.slice(0, -4);
      return url.toString();
    } catch (e) {
      console.error(
        `[GitHub Loader]: Error processing repository URL ${this.repo}: ${e.message}`
      );
      return repoUrl;
    }
  }

  
  #validGithubUrl() {
    try {
      const url = new URL(this.repo);

      if (url.hostname !== "github.com") {
        return false;
      }

      const [author, project, ..._rest] = url.pathname.slice(1).split("/");
      if (!author || !project) {
        return false;
      }

      this.author = author;
      this.project = project;
      return true;
    } catch (e) {
      return false;
    }
  }

  async #validBranch() {
    await this.getRepoBranches();
    if (!!this.branch && this.branches.includes(this.branch)) return;
    this.branch = this.branches.includes("main") ? "main" : "master";
    return;
  }

  async #validateAccessToken() {
    if (!this.accessToken) return;
    const valid = await fetch("https://api.github.com/octocat", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${this.accessToken}`,
        "X-GitHub-Api-Version": "2022-11-28",
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error(res.statusText);
        return res.ok;
      })
      .catch((e) => {
        console.error(
          "Invalid GitHub Access Token provided! Access token will not be used",
          e.message
        );
        return false;
      });

    if (!valid) this.accessToken = null;
    return;
  }

  
  async init() {
    if (!this.#validGithubUrl()) return;
    await this.#validBranch();
    await this.#validateAccessToken();
    this.ready = true;
    return this;
  }

  
  async recursiveLoader() {
    if (!this.ready) throw new Error("[GitHub Loader]: not in ready state!");
    const {
      GithubRepoLoader: LCGithubLoader,
    } = require("@langchain/community/document_loaders/web/github");

    if (this.accessToken)

    const loader = new LCGithubLoader(this.repo, {
      branch: this.branch,
      recursive: !!this.accessToken, // Recursive will hit rate limits.
      maxConcurrency: 5,
      unknown: "warn",
      accessToken: this.accessToken,
      ignorePaths: this.ignorePaths,
      verbose: true,
    });

    const docs = await loader.load();
    return docs;
  }

  #branchPrefSort(branches = []) {
    const preferredSort = ["main", "master"];
    return branches.reduce((acc, branch) => {
      if (preferredSort.includes(branch)) return [branch, ...acc];
      return [...acc, branch];
    }, []);
  }

  
  async getRepoBranches() {
    if (!this.#validGithubUrl() || !this.author || !this.project) return [];
    await this.#validateAccessToken(); // Ensure API access token is valid for pre-flight

    let page = 0;
    let polling = true;
    const branches = [];

    while (polling) {
      await fetch(
        `https://api.github.com/repos/${this.author}/${this.project}/branches?per_page=100&page=${page}`,
        {
          method: "GET",
          headers: {
            ...(this.accessToken
              ? { Authorization: `Bearer ${this.accessToken}` }
              : {}),
            "X-GitHub-Api-Version": "2022-11-28",
          },
        }
      )
        .then((res) => {
          if (res.ok) return res.json();
          throw new Error(`Invalid request to Github API: ${res.statusText}`);
        })
        .then((branchObjects) => {
          polling = branchObjects.length > 0;
          branches.push(branchObjects.map((branch) => branch.name));
          page++;
        })
        .catch((err) => {
          polling = false;
        });
    }

    this.branches = [...new Set(branches.flat())];
    return this.#branchPrefSort(this.branches);
  }

  
  async fetchSingleFile(sourceFilePath) {
    try {
      return fetch(
        `https://api.github.com/repos/${this.author}/${this.project}/contents/${sourceFilePath}?ref=${this.branch}`,
        {
          method: "GET",
          headers: {
            Accept: "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
            ...(!!this.accessToken
              ? { Authorization: `Bearer ${this.accessToken}` }
              : {}),
          },
        }
      )
        .then((res) => {
          if (res.ok) return res.json();
          throw new Error(`Failed to fetch from Github API: ${res.statusText}`);
        })
        .then((json) => {
          if (json.hasOwnProperty("status") || !json.hasOwnProperty("content"))
            throw new Error(json?.message || "missing content");
          return atob(json.content);
        });
    } catch (e) {
      console.error(`RepoLoader.fetchSingleFile`, e);
      return null;
    }
  }
}

module.exports = GitHubRepoLoader;
