const httpLogger =
  ({ enableTimestamps = false }) =>
  (req, res, next) => {
    const originalEnd = res.end;

    res.end = function (chunk, encoding) {
      const statusColor = res.statusCode >= 400 ? "\x1b[31m" : "\x1b[32m"; // Red for errors, green for success.toLocaleTimeString("en-US", { hour12: true })}` : ""}`.trim()
      );

      // Call the original end method
      return originalEnd.call(this, chunk, encoding);
    };

    next();
  };

module.exports = {
  httpLogger,
};
