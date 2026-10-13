
async function patchShellEnvironmentPath() {
  try {
    if (process.platform === "win32") return process.env;
    const { default: fixPath } = await import("fix-path");
    const { default: stripAnsi } = await import("strip-ansi");
    fixPath();
    if (process.env.PATH) process.env.PATH = stripAnsi(process.env.PATH);
    return process.env;
  } catch (error) {
    console.error("Failed to patch shell environment path:", error);
    return process.env;
  }
}

module.exports = {
  patchShellEnvironmentPath,
};
