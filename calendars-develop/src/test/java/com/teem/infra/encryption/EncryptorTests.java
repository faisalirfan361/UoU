package com.UoU.infra.encryption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU.core.SecretString;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.KeyGenerator;
import org.junit.jupiter.api.Test;

class EncryptorTests {
  private static final Encryptor ENCRYPTOR = new Encryptor(createConfig());

  @Test
  public void ctor_shouldThrowWithEmptyOrNonBase64Key() {
    assertThatCode(() -> new Encryptor(null))
        .isInstanceOf(InvalidKeyException.class);
    assertThatCode(() -> new Encryptor(new Config(new SecretString(""))))
        .isInstanceOf(InvalidKeyException.class);
    assertThatCode(() -> new Encryptor(new Config(new SecretString("not-base64"))))
        .isInstanceOf(InvalidKeyException.class);
  }

  @Test
  public void encrypt_decrypt_shouldRoundTripBytes() {
    var input = "This is some very texty text.";
    var inputBytes = input.getBytes(StandardCharsets.UTF_8);

    var encryptedFromBytes = ENCRYPTOR.encrypt(inputBytes);
    var decryptedFromBytes = ENCRYPTOR.decrypt(encryptedFromBytes);

    assertThat(encryptedFromBytes).isNotEqualTo(inputBytes);
    assertThat(decryptedFromBytes).isEqualTo(inputBytes);
  }

  @Test
  public void encrypt_decrypt_shouldRoundTripString() {
    var input = "Hello there ☺";
    var inputBytes = input.getBytes(StandardCharsets.UTF_8);

    var encryptedFromString = ENCRYPTOR.encrypt(input);
    var decryptedFromString = ENCRYPTOR.decrypt(encryptedFromString);

    assertThat(encryptedFromString).isNotEqualTo(inputBytes);
    assertThat(decryptedFromString).isEqualTo(inputBytes);
    assertThat(new String(decryptedFromString, StandardCharsets.UTF_8)).isEqualTo(input);
  }

  @Test
  public void encrypt_decryptToString_shouldRoundTripString() {
    var input = "Hello there, I'm a string ☺";

    var encryptedFromString = ENCRYPTOR.encrypt(input);
    var decryptedFromString = ENCRYPTOR.decryptToString(encryptedFromString);

    assertThat(decryptedFromString).isEqualTo(input);
  }

  @Test
  public void encrypt_decrypt_shouldThrowWhenKeyIsWrongLengthForAes() {
    var config = new Config(
        new SecretString(Base64.getEncoder().encodeToString("too-short".getBytes())));
    var encryptor = new Encryptor(config);

    assertThatCode(() -> encryptor.encrypt("test-encrypted-value"))
        .isInstanceOf(InvalidKeyException.class);
    assertThatCode(() -> encryptor.decrypt("test-encrypted-value".getBytes()))
        .isInstanceOf(InvalidKeyException.class);
  }

  @Test
  public void decrypt_shouldThrowWithWrongKey() {
    var encrypted = ENCRYPTOR.encrypt("hi");
    var encryptorWithWrongKey = new Encryptor(createConfig());

    assertThatCode(() -> encryptorWithWrongKey.decrypt(encrypted))
        .isInstanceOf(AEADBadTagException.class);
  }

  private static Config createConfig() {
    KeyGenerator generator;
    try {
      generator = KeyGenerator.getInstance("AES");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    generator.init(256);
    var key = Base64.getEncoder().encodeToString(generator.generateKey().getEncoded());

    return new Config(new SecretString(key));
  }
}
