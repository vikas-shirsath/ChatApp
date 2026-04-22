// E2EE encryption using Web Crypto API (no external deps)

// --- Key Generation ---

export async function generateKeyPair() {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: 'RSA-OAEP',
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: 'SHA-256',
    },
    true,
    ['encrypt', 'decrypt']
  );

  const publicKeyBuffer = await window.crypto.subtle.exportKey('spki', keyPair.publicKey);
  const privateKeyBuffer = await window.crypto.subtle.exportKey('pkcs8', keyPair.privateKey);

  return {
    publicKey: bufferToBase64(publicKeyBuffer),
    privateKey: bufferToBase64(privateKeyBuffer),
  };
}

// --- Import Keys ---

async function importPublicKey(base64) {
  const buffer = base64ToBuffer(base64);
  return window.crypto.subtle.importKey(
    'spki',
    buffer,
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['encrypt']
  );
}

async function importPrivateKey(base64) {
  const buffer = base64ToBuffer(base64);
  return window.crypto.subtle.importKey(
    'pkcs8',
    buffer,
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['decrypt']
  );
}

// --- Encrypt a message ---
// Returns { encryptedPayload, encryptedKey } as base64 strings

export async function encryptMessage(plaintext, receiverPublicKeyBase64) {
  // 1. Generate random AES-256-GCM key
  const aesKey = await window.crypto.subtle.generateKey(
    { name: 'AES-GCM', length: 256 },
    true,
    ['encrypt', 'decrypt']
  );

  // 2. Encrypt plaintext with AES
  const iv = window.crypto.getRandomValues(new Uint8Array(12));
  const encoded = new TextEncoder().encode(plaintext);
  const ciphertext = await window.crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    aesKey,
    encoded
  );

  // 3. Combine IV + ciphertext
  const combined = new Uint8Array(iv.length + ciphertext.byteLength);
  combined.set(iv);
  combined.set(new Uint8Array(ciphertext), iv.length);

  // 4. Encrypt the AES key with receiver's RSA public key
  const rawAesKey = await window.crypto.subtle.exportKey('raw', aesKey);
  const rsaPubKey = await importPublicKey(receiverPublicKeyBase64);
  const encryptedAesKey = await window.crypto.subtle.encrypt(
    { name: 'RSA-OAEP' },
    rsaPubKey,
    rawAesKey
  );

  return {
    encryptedPayload: bufferToBase64(combined.buffer),
    encryptedKey: bufferToBase64(encryptedAesKey),
  };
}

// --- Decrypt a message ---

export async function decryptMessage(encryptedPayloadBase64, encryptedKeyBase64, privateKeyBase64) {
  try {
    // 1. Decrypt AES key with our RSA private key
    const rsaPrivKey = await importPrivateKey(privateKeyBase64);
    const encryptedAesKeyBuffer = base64ToBuffer(encryptedKeyBase64);
    const rawAesKey = await window.crypto.subtle.decrypt(
      { name: 'RSA-OAEP' },
      rsaPrivKey,
      encryptedAesKeyBuffer
    );

    // 2. Import AES key
    const aesKey = await window.crypto.subtle.importKey(
      'raw',
      rawAesKey,
      { name: 'AES-GCM', length: 256 },
      false,
      ['decrypt']
    );

    // 3. Extract IV and ciphertext
    const combined = base64ToBuffer(encryptedPayloadBase64);
    const iv = new Uint8Array(combined, 0, 12);
    const ciphertext = new Uint8Array(combined, 12);

    // 4. Decrypt
    const decrypted = await window.crypto.subtle.decrypt(
      { name: 'AES-GCM', iv },
      aesKey,
      ciphertext
    );

    return new TextDecoder().decode(decrypted);
  } catch (err) {
    console.warn('Decryption failed:', err.message);
    return null;
  }
}

// --- Helpers ---

function bufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

function base64ToBuffer(base64) {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}
