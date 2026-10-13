const crypto = require("crypto");
const fs = require("fs");
const path = require("path");
const keyPath =
  process.env.NODE_ENV === "development"
    ? path.resolve(__dirname, `../../storage/comkey`)
    : path.resolve(
        process.env.STORAGE_DIR ?? path.resolve(__dirname, `../../storage`),
        `comkey`
      );

// What does this class do?
// This class generates a hashed version of some text (typically a JSON payload) using a rolling RSA key
// nature of this class and that keys are rolled constantly, this protects the request
// This keeps accidental misconfigurations of AnythingLLM that leaving port 8888 open from
// Since each request to the collector must be signed to be valid, unsigned requests directly to the collector
class CommunicationKey {
  #privKeyName = "ipc-priv.pem";
  #pubKeyName = "ipc-pub.pem";
  #storageLoc = keyPath;

  constructor(generate = false) {
    if (generate) this.#generate();
  }

  log(text, ...args) {
  }

  #readPrivateKey() {
    return fs.readFileSync(path.resolve(this.#storageLoc, this.#privKeyName));
  }

  #generate() {
    const keyPair = crypto.generateKeyPairSync("rsa", {
      modulusLength: 2048,
      publicKeyEncoding: {
        type: "pkcs1",
        format: "pem",
      },
      privateKeyEncoding: {
        type: "pkcs1",
        format: "pem",
      },
    });

    if (!fs.existsSync(this.#storageLoc))
      fs.mkdirSync(this.#storageLoc, { recursive: true });
    fs.writeFileSync(
      `${path.resolve(this.#storageLoc, this.#privKeyName)}`,
      keyPair.privateKey
    );
    fs.writeFileSync(
      `${path.resolve(this.#storageLoc, this.#pubKeyName)}`,
      keyPair.publicKey
    );
    this.log(
      "RSA key pair generated for signed payloads within AnythingLLM services."
    );
  }

  // this resource is shared with /collector/ via a class of the same name in /utils which does decoding/verification only
  sign(textData = "") {
    return crypto
      .sign("RSA-SHA256", Buffer.from(textData), this.#readPrivateKey())
      .toString("hex");
  }

  encrypt(textData = "") {
    return crypto
      .privateEncrypt(this.#readPrivateKey(), Buffer.from(textData, "utf-8"))
      .toString("base64");
  }
}

module.exports = { CommunicationKey };
