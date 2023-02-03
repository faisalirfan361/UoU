package com.UoU.infra.encryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * Service for app-level encryption and decryption of values for caching, storage, etc.
 *
 * <p>This uses AES/GCM/NoPadding for symmetric encryption with a secret key.
 *
 * <p>Encrypted values are prefixed with a 12-byte IV/nonce like: [12-byte IV] + [encrypted value].
 * Therefore, to decrypt values, the first 12-bytes are removed and used as the IV. If you're using
 * this class to both encrypt and decrypt values, this all happens behind the scenes automatically.
 */
@Service
public class Encryptor {
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int IV_LENGTH_BYTES = 12;
  private static final int IV_LENGTH_BITS = IV_LENGTH_BYTES * 8;

  private final SecretKeySpec key;
  private final SecureRandom random;

  @SneakyThrows
  Encryptor(Config config) {
    if (config == null) {
      throw new InvalidKeyException("Encryptor config with key cannot be empty");
    }

    try {
      this.key = new SecretKeySpec(
          Base64.getDecoder().decode(config.secretKey().value()),
          KEY_ALGORITHM);
    } catch (IllegalArgumentException ex) {
      throw new InvalidKeyException("Encryptor key must be base-64 encoded");
    }

    this.random = new SecureRandom();
  }

  /**
   * Encrypts the value and returns the encrypted bytes.
   */
  @SneakyThrows
  public byte[] encrypt(byte[] value) {
    var iv = generateIv();
    var cipher = initCipher(
        Cipher.ENCRYPT_MODE,
        new GCMParameterSpec(IV_LENGTH_BITS, iv));

    var encryptedBytes = cipher.doFinal(value);

    // Return iv prefix + actual value. When decrypted, the iv prefix will be removed.
    // The iv is random and increases security by making the encrypted value non-deterministic.
    return ByteBuffer.allocate(IV_LENGTH_BYTES + encryptedBytes.length)
        .put(iv)
        .put(encryptedBytes)
        .array();
  }

  /**
   * Shortcut for getting the string's bytes with UTF-8 and then calling {@link #encrypt(byte[])}.
   */
  public byte[] encrypt(String value) {
    return encrypt(value.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decrypts the bytes and returns the unencrypted bytes.
   */
  @SneakyThrows
  public byte[] decrypt(byte[] value) {
    var cipher = initCipher(
        Cipher.DECRYPT_MODE,
        new GCMParameterSpec(IV_LENGTH_BITS, value, 0, IV_LENGTH_BYTES));

    // Encrypted value is prefixed with iv, so remove the prefix to get the actual value.
    // The iv is random and increases security by making the encrypted value non-deterministic.
    var actualValue = Arrays.copyOfRange(value, IV_LENGTH_BYTES, value.length);

    return cipher.doFinal(actualValue);
  }

  /**
   * Shortcut that decrypts the bytes via {@link #decrypt(byte[])} and creates a UTF-8 string.
   */
  public String decryptToString(byte[] value) {
    return new String(decrypt(value), StandardCharsets.UTF_8);
  }

  @SneakyThrows
  private Cipher initCipher(int mode, GCMParameterSpec params) {
    var cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(mode, key, params);
    return cipher;
  }

  /**
   * Generates a secure-random IV/nonce to make encrypted values non-deterministic and more secure.
   *
   * <p>A GCM IV/nonce is usually 12 bytes or 96 bits, so we stick to that standard.
   */
  private byte[] generateIv() {
    var bytes = new byte[IV_LENGTH_BYTES];
    random.nextBytes(bytes);
    return bytes;
  }
}
