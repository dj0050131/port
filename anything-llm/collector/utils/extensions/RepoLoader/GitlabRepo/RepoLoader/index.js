const ignore = require("ignore");





/**
 * @class
 * @classdesc Loads and manages GitLab repository content.
 */
class GitLabRepoLoader {
  /**
   * Creates an instance of RepoLoader.
   * @param {RepoLoaderArgs} [args] - The configuration options.
   * @returns {GitLabRepoLoader}
   */
  constructor(args = {}) {
    this.ready = false;
    this.repo = args?.repo;
    this.branch = args?.branch;
    this.accessToken = args?.accessToken || null;
    this.ignorePaths = args?.ignorePaths || [];
    this.ignoreFilter = ignore().add(this.ignorePaths);
    this.withIssues = args?.fetchIssues || false;
    this.withWikis = args?.fetchWikis || false;

    this.projectId = null;
    this.apiBase = "https://gitlab.com";
    this.author = null;
    this.project = null;
    this.branches = [];
  }

  #validGitlabUrl() {
    const validPatterns = [
      /https:\/\/gitlab\.com\/(?<author>[^\/]+)\/(?<project>.*)/,
      // This should even match the regular hosted URL, but we may want to know
      /(http|https):\/\/[^\/]+\/(?<author>[^\/]+)\/(?<project>.*)/,
    ];

    const match = validPatterns
      .find((pattern) => this.repo.match(pattern)?.groups)
      ?.exec(this.repo);
    if (!match?.groups) return false;

    const { author, project } = match.groups;
    this.projectId = encodeURIComponent(`${author}/${project}`);
    this.apiBase = new URL(this.repo).origin;
    this.author = author;
    this.project = project;
    return true;
  }

  async #validBranch() {
    await this.getRepoBranches();
    if (!!this.branch && this.branches.includes(this.branch)) return;
    this.branch = this.branches.includes("main") ? "main" : "master";
    return;
  }

  async #validateAccessToken() {
    if (!this.accessToken) return;
    try {
      await fetch(`${this.apiBase}/api/v4/user`, {
        method: "GET",
        headers: this.accessToken ? { "PRIVATE-TOKEN": this.accessToken } : {},
      }).then((res) => res.ok);
    } catch (e) {
      console.error(
        "Invalid Gitlab Access Token provided! Access token will not be used",
        e.message
      );
      this.accessToken = null;
    }
  }

  
  async init() {
    if (!this.#validGitlabUrl()) return;
    await this.#validBranch();
    await this.#validateAccessToken();
    this.ready = true;
    return this;
  }

  
  async recursiveLoader() {
    if (!this.ready) throw new Error("[Gitlab Loader]: not in ready state!");

    if (this.accessToken)

    const docs = [];

    const files = await this.fetchFilesRecursive();

    for (const file of files) {
      if (this.ignoreFilter.ignores(file.path)) continue;

      docs.push({
        pageContent: file.content,
        metadata: {
          source: file.path,
          url: `${this.repo}/-/blob/${this.branch}/${file.path}`,
        },
      });
    }

    if (this.withIssues) {
      const issues = await this.fetchIssues();
      docs.push(
        ...issues.map((issue) => ({
          issue,
          metadata: {
            source: `issue-${this.repo}-${issue.iid}`,
            url: issue.web_url,
          },
        }))
      );
    }

    if (this.withWikis) {
      const wiki = await this.fetchWiki();
      docs.push(
        ...wiki.map((wiki) => ({
          wiki,
          metadata: {
            source: `wiki-${this.repo}-${wiki.slug}`,
            url: `${this.repo}/-/wikis/${wiki.slug}`,
          },
        }))
      );
    }

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
    if (!this.#validGitlabUrl() || !this.projectId) return [];
    await this.#validateAccessToken();
    this.branches = [];

    const branchesRequestData = {
      endpoint: `/api/v4/projects/${this.projectId}/repository/branches`,
    };

    let branchesPage = [];
    while ((branchesPage = await this.fetchNextPage(branchesRequestData))) {
      if (!Array.isArray(branchesPage) || !branchesPage?.length) break;
      this.branches.push(...branchesPage.map((branch) => branch.name));
    }
    return this.#branchPrefSort(this.branches);
  }

  
  async fetchFilesRecursive() {
    const files = [];
    const filesRequestData = {
      endpoint: `/api/v4/projects/${this.projectId}/repository/tree`,
      queryParams: {
        ref: this.branch,
        recursive: true,
      },
    };

    let filesPage = null;
    let pagePromises = [];
    while ((filesPage = await this.fetchNextPage(filesRequestData))) {
      if (!Array.isArray(filesPage) || !filesPage?.length) break;
      pagePromises = filesPage
        .filter((file) => {
          if (file.type !== "blob") return false;
          return !this.ignoreFilter.ignores(file.path);
        })
        .map(async (file) => {
          const content = await this.fetchSingleFileContents(file.path);
          if (!content) return null;
          return {
            path: file.path,
            content,
          };
        });

      const pageFiles = await Promise.all(pagePromises);

      files.push(...pageFiles.filter((item) => item !== null));
    }
    return files;
  }

  
  async fetchIssues() {
    const issues = [];
    const issuesRequestData = {
      endpoint: `/api/v4/projects/${this.projectId}/issues`,
    };

    let issuesPage = null;
    let pagePromises = [];
    while ((issuesPage = await this.fetchNextPage(issuesRequestData))) {
      if (!Array.isArray(issuesPage) || !issuesPage?.length) break;
      pagePromises = issuesPage.map(async (issue) => {
        const discussionsRequestData = {
          endpoint: `/api/v4/projects/${this.projectId}/issues/${issue.iid}/discussions`,
        };
        let discussionPage = null;
        const discussions = [];

        while (
          (discussionPage = await this.fetchNextPage(discussionsRequestData))
        ) {
          if (!Array.isArray(discussionPage) || !discussionPage?.length) break;
          discussions.push(
            ...discussionPage.map(({ notes }) =>
              notes.map(
                ({ body, author, created_at }) =>
                  `${author.username} at ${created_at}:
${body}`
              )
            )
          );
        }
        const result = {
          ...issue,
          discussions,
        };
        return result;
      });

      const pageIssues = await Promise.all(pagePromises);

      issues.push(...pageIssues);
    }
    return issues;
  }

  
  async fetchWiki() {
    const wikiRequestData = {
      endpoint: `/api/v4/projects/${this.projectId}/wikis`,
      queryParams: {
        with_content: "1",
      },
    };

    const wikiPages = await this.fetchNextPage(wikiRequestData);
    if (!Array.isArray(wikiPages)) return [];
    return wikiPages;
  }

  
  async fetchSingleFileContents(sourceFilePath) {
    try {
      const data = await fetch(
        `${this.apiBase}/api/v4/projects/${
          this.projectId
        }/repository/files/${encodeURIComponent(sourceFilePath)}/raw?ref=${
          this.branch
        }`,
        {
          method: "GET",
          headers: this.accessToken
            ? { "PRIVATE-TOKEN": this.accessToken }
            : {},
        }
      ).then((res) => {
        if (res.ok) return res.text();
        throw new Error(`Failed to fetch single file ${sourceFilePath}`);
      });

      return data;
    } catch (e) {
      console.error(`RepoLoader.fetchSingleFileContents`, e);
      return null;
    }
  }

  
  async fetchNextPage(requestData) {
    try {
      if (requestData.page === -1) return null;
      if (!requestData.page) requestData.page = 1;

      const { endpoint, perPage = 100, queryParams = {} } = requestData;
      const params = new URLSearchParams({
        ...queryParams,
        per_page: perPage,
        page: requestData.page,
      });
      const url = `${this.apiBase}${endpoint}?${params.toString()}`;

      const response = await fetch(url, {
        method: "GET",
        headers: this.accessToken ? { "PRIVATE-TOKEN": this.accessToken } : {},
      });

      if (response.status === 401) {
        console.warn(`Rate limit hit for ${endpoint}. Skipping.`);
        return null;
      }

      const totalPages = Number(response.headers.get("x-total-pages"));
      const data = await response.json();
      if (!Array.isArray(data)) {
        console.warn(`Unexpected response format for ${endpoint}:`, data);
        return [];
      }

      if (totalPages === requestData.page) {
        requestData.page = -1;
      } else {
        requestData.page = Number(response.headers.get("x-next-page"));
      }

      return data;
    } catch (e) {
      console.error(`RepoLoader.fetchNextPage`, e);
      return null;
    }
  }
}

module.exports = GitLabRepoLoader;
