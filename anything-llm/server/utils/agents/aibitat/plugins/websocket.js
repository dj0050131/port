const chalk = require("chalk");
const { Telemetry } = require("../../../../models/telemetry");
const SOCKET_TIMEOUT_MS = 300 * 1_000; // 5 mins

/**
 * Websocket Interface plugin. It prints the messages on the console and asks for feedback
 * while the conversation is running in the background.
 */

//   askForFeedback?: any
//   awaitResponse?: any
// }

const WEBSOCKET_BAIL_COMMANDS = [
  "exit",
  "/exit",
  "stop",
  "/stop",
  "halt",
  "/halt",
  "/reset", // Will not reset but will bail. Powerusers always do this and the LLM responds.
];
const websocket = {
  name: "websocket",
  startupConfig: {
    params: {
      socket: {
        required: true,
      },
      muteUserReply: {
        required: false,
        default: true,
      },
      introspection: {
        required: false,
        default: true,
      },
    },
  },
  plugin: function ({
    socket, // @type AIbitatWebSocket
    muteUserReply = true, // Do not post messages to "USER" back to frontend.
    introspection = false, // when enabled will attach socket to Aibitat object with .introspect method which reports status updates to frontend.
  }) {
    return {
      name: this.name,
      setup(aibitat) {
        aibitat.onError(async (error) => {
          let errorMessage =
            error?.message || "An error occurred while running the agent.";
          console.error(chalk.red(`   error: ${errorMessage}`), error);
          aibitat.introspect(
            `Error encountered while running: ${errorMessage}`
          );
          socket.send(
            JSON.stringify({ type: "wssFailure", content: errorMessage })
          );
          aibitat.terminate();
        });

        aibitat.introspect = (messageText) => {
          if (!introspection) return; // Dump thoughts when not wanted.
          socket.send(
            JSON.stringify({
              type: "statusResponse",
              content: messageText,
              animate: true,
            })
          );
        };

        // expose function for sockets across aibitat
        aibitat.socket = {
          send: (type = "__unhandled", content = "") => {
            socket.send(JSON.stringify({ type, content }));
          },
        };

        // });

        aibitat.onMessage((message) => {
          if (message.from !== "USER")
            Telemetry.sendTelemetry("agent_chat_sent");
          if (message.from === "USER" && muteUserReply) return;
          socket.send(JSON.stringify(message));
        });

        aibitat.onTerminate(() => {
          socket.close();
        });

        aibitat.onInterrupt(async (node) => {
          const feedback = await socket.askForFeedback(socket, node);
          if (WEBSOCKET_BAIL_COMMANDS.includes(feedback)) {
            socket.close();
            return;
          }

          await aibitat.continue(feedback);
        });

        
        socket.askForFeedback = (socket, node) => {
          socket.awaitResponse = (question = "waiting...") => {
            socket.send(JSON.stringify({ type: "WAITING_ON_INPUT", question }));

            return new Promise(function (resolve) {
              let socketTimeout = null;
              socket.handleFeedback = (message) => {
                const data = JSON.parse(message);
                if (data.type !== "awaitingFeedback") return;
                delete socket.handleFeedback;
                clearTimeout(socketTimeout);
                resolve(data.feedback);
                return;
              };

              socketTimeout = setTimeout(() => {
                );
                resolve("exit");
                return;
              }, SOCKET_TIMEOUT_MS);
            });
          };

          return socket.awaitResponse(`Provide feedback to ${chalk.yellow(
            node.to
          )} as ${chalk.yellow(node.from)}.
           Press enter to skip and use auto-reply, or type 'exit' to end the conversation: \n`);
        };
      },
    };
  },
};

module.exports = {
  websocket,
  WEBSOCKET_BAIL_COMMANDS,
};
