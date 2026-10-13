const { input } = require("@inquirer/prompts");
const chalk = require("chalk");
const { RetryError } = require("../error");

/**
 * Command-line Interface plugin. It prints the messages on the console and asks for feedback
 * while the conversation is running in the background.
 */
const cli = {
  name: "cli",
  startupConfig: {
    params: {},
  },
  plugin: function ({ simulateStream = true } = {}) {
    return {
      name: this.name,
      setup(aibitat) {
        let printing = [];

        aibitat.onError(async (error) => {
          let errorMessage =
            error?.message || "An error occurred while running the agent.";
          console.error(chalk.red(`   error: ${errorMessage}`), error);
        });

        aibitat.onStart(() => {
          printing = [Promise.resolve()];
        });

        aibitat.onMessage(async (message) => {
          const next = new Promise(async (resolve) => {
            await Promise.all(printing);
            await this.print(message, simulateStream);
            resolve();
          });
          printing.push(next);
        });

        aibitat.onTerminate(async () => {
          await Promise.all(printing);
        });

        aibitat.onInterrupt(async (node) => {
          await Promise.all(printing);
          const feedback = await this.askForFeedback(node);
          // Add an extra line after the message

          if (feedback === "exit") {
            return process.exit(0);
          }

          await aibitat.continue(feedback);
        });
      },

      
      print: async function (message = {}, simulateStream = true) {
        const replying = chalk.dim(`(to ${message.to})`);
        const reference = `${chalk.magenta("✎")} ${chalk.bold(
          message.from
        )} ${replying}:`;

        if (!simulateStream) {
          // Add an extra line after the message
          return;
        }

        process.stdout.write(`${reference}\n`);

        const chunks = message.content?.split(" ") || [];
        const stream = new ReadableStream({
          async start(controller) {
            for (const chunk of chunks) {
              const bytes = new TextEncoder().encode(chunk + " ");
              controller.enqueue(bytes);
              await new Promise((r) =>
                setTimeout(
                  r,
                  // get a random number between 10ms and 50ms to simulate a random delay
                  Math.floor(Math.random() * 40) + 10
                )
              );
            }
            controller.close();
          },
        });

        for await (const chunk of stream) {
          process.stdout.write(new TextDecoder().decode(chunk));
        }

        // Add an extra line after the message
      },

      
      askForFeedback: function (node = {}) {
        return input({
          message: `Provide feedback to ${chalk.yellow(
            node.to
          )} as ${chalk.yellow(
            node.from
          )}. Press enter to skip and use auto-reply, or type 'exit' to end the conversation: `,
        });
      },
    };
  },
};

module.exports = { cli };
