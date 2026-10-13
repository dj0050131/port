
function chatHistoryViewable(_request, response, next) {
  if ("DISABLE_VIEW_CHAT_HISTORY" in process.env)
    return response
      .status(422)
      .send("This feature has been disabled by the administrator.");
  next();
}

module.exports = {
  chatHistoryViewable,
};
