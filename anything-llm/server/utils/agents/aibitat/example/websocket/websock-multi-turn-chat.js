
const express = require("express");
const chalk = require("chalk");
const AIbitat = require("../../index.js");
const {
  websocket,
  webBrowsing,
  webScraping,
} = require("../../plugins/index.js");
const path = require("path");
const port = 3000;
const app = express();
require("@mintplex-labs/express-ws").default(app); // load WebSockets in non-SSL mode.
require("dotenv").config({ path: `../../../../../.env.development` });

// Debugging echo function if this is working for you.
//   });
// });

app.ws("/ws", function (ws, _response) {
  try {
    ws.on("message", function (msg) {
      if (ws?.handleFeedback) ws.handleFeedback(msg);
    });

    ws.on("close", function () {
      return;
    });
    runAIbitat(ws).catch((error) => {
      ws.send(
        JSON.stringify({
          from: Agent.AI,
          to: Agent.HUMAN,
          content: error.message,
        })
      );
    });
  } catch (error) {}
});

app.all("*", function (_, response) {
  response.sendFile(path.join(__dirname, "index.html"));
});

app.listen(port, () => {
});

const Agent = {
  HUMAN: "🧑",
  AI: "🤖",
};

async function runAIbitat(socket) {
  if (!process.env.OPEN_AI_KEY)
    throw new Error(
      "This example requires a valid OPEN_AI_KEY in the env.development file"
    );"));
  const aibitat = new AIbitat({
    provider: "openai",
    model: "gpt-4o",
  })
    .use(websocket.plugin({ socket }))
    .use(webBrowsing.plugin())
    .use(webScraping.plugin())
    .agent(Agent.HUMAN, {
      interrupt: "ALWAYS",
      role: "You are a human assistant.",
    })
    .agent(Agent.AI, {
      role: "You are a helpful ai assistant who likes to chat with the user who an also browse the web for questions it does not know or have real-time access to.",
      functions: ["web-browsing"],
    });

  await aibitat.start({
    from: Agent.HUMAN,
    to: Agent.AI,
    content: `How are you doing today?`,
  });
}
