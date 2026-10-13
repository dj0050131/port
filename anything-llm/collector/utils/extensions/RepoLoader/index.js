
function resolveRepoLoader(platform = "github") {
  switch (platform) {
    case "github":
      return require("./GithubRepo/RepoLoader");
    case "gitlab":
      return require("./GitlabRepo/RepoLoader");
    default:
      return require("./GithubRepo/RepoLoader");
  }
}


function resolveRepoLoaderFunction(platform = "github") {
  switch (platform) {
    case "github":
      return require("./GithubRepo").loadGithubRepo;
    case "gitlab":
      return require("./GitlabRepo").loadGitlabRepo;
    default:
      return require("./GithubRepo").loadGithubRepo;
  }
}

module.exports = { resolveRepoLoader, resolveRepoLoaderFunction };
