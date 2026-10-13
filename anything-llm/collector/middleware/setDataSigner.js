const { EncryptionWorker } = require("../utils/EncryptionWorker");
const { CommunicationKey } = require("../utils/comKey");



// You can use this middleware to assign the EncryptionWorker to the response locals

// The way this functions is that the rolling RSA Communication Key is used server-side to private-key encrypt the raw
// even between server<>collector in plaintext because if the user configured the server/collector to be public they could technically

// This middleware uses the CommunicationKey public key to first decrypt the base64 representation of the EncryptionManager credentials


function setDataSigner(request, response, next) {
  const comKey = new CommunicationKey();
  const encryptedPayloadSigner = request.header("X-Payload-Signer");
  if (!encryptedPayloadSigner) console.log('Failed to find signed-payload to set encryption worker! Encryption calls will fail.');

  const decryptedPayloadSignerKey = comKey.decrypt(encryptedPayloadSigner);
  const encryptionWorker = new EncryptionWorker(decryptedPayloadSignerKey);
  response.locals.encryptionWorker = encryptionWorker;
  next();
}

module.exports = {
  setDataSigner
}