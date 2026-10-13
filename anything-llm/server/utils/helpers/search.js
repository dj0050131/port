const { Workspace } = require("../../models/workspace");
const { WorkspaceThread } = require("../../models/workspaceThread");
const fastLevenshtein = require("fast-levenshtein");

const FAST_LEVENSHTEIN_DISTANCE = 3;


async function searchWorkspaceAndThreads(searchTerm, user = null) {
  searchTerm = String(searchTerm).trim(); // Ensure searchTerm is a string and trimmed.

  if (!searchTerm || searchTerm.length < 3)
    return { workspaces: [], threads: [] };
  searchTerm = searchTerm.toLowerCase();

  const results = {
    workspaces: new Set(),
    threads: new Set(),
  };

  async function searchWorkspaces() {
    const workspaces = !!user
      ? await Workspace.whereWithUser(user)
      : await Workspace.where();

    for (const workspace of workspaces) {
      const wsName = workspace.name.toLowerCase();
      if (
        wsName.startsWith(searchTerm) ||
        wsName.includes(searchTerm) ||
        wsName.endsWith(searchTerm) ||
        fastLevenshtein.get(wsName, searchTerm) <= FAST_LEVENSHTEIN_DISTANCE
      )
        results.workspaces.add(
          JSON.stringify({ slug: workspace.slug, name: workspace.name })
        );
    }
  }

  async function searchThreads() {
    const threads = !!user
      ? await WorkspaceThread.where(
          { user_id: user.id },
          undefined,
          undefined,
          { workspace: { select: { slug: true, name: true } } }
        )
      : await WorkspaceThread.where(undefined, undefined, undefined, {
          workspace: { select: { slug: true, name: true } },
        });

    for (const thread of threads) {
      const threadName = thread.name.toLowerCase();
      if (
        threadName.startsWith(searchTerm) ||
        threadName.includes(searchTerm) ||
        threadName.endsWith(searchTerm) ||
        fastLevenshtein.get(threadName, searchTerm) <= FAST_LEVENSHTEIN_DISTANCE
      )
        results.threads.add(
          JSON.stringify({
            slug: thread.slug,
            name: thread.name,
            workspace: {
              slug: thread.workspace.slug,
              name: thread.workspace.name,
            },
          })
        );
    }
  }

  await Promise.all([searchWorkspaces(), searchThreads()]);

  const workspaces = Array.from(results.workspaces).map(JSON.parse);
  const threads = Array.from(results.threads).map(JSON.parse);
  return { workspaces, threads };
}

module.exports = { searchWorkspaceAndThreads };
