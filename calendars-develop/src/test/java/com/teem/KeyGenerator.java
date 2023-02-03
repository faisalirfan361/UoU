package com.UoU;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Helper to generate various keys. This is not actually a test. Enable to run when needed.
 *
 * <p>DO-MAYBE: Put these scripts in a better place. But this works for now.
 */
class KeyGenerator {

  /**
   * Generates a public JWK and private JWK for public/private RS256 JWT validation.
   *
   * <p>Output files:
   * - {root}/public-jwk.key.json -> Goes into config "jwt.public-jwk" value
   * - {root}/private-jwks.key.json -> Can be used to generate and sign JWTs
   */
  @Test
  @Disabled("Not actually a test. Enable when needed.")
  @SneakyThrows
  void generateJwks() {
    var generator = new RSAKeyGenerator(2048);
    var key = generator.generate();
    var jwk = JWK.parse(key.toJSONObject());

    try (var output = new FileOutputStream("public-jwk.key.json")) {
      output.write(jwk.toPublicJWK().toJSONString().getBytes(StandardCharsets.UTF_8));
    }

    try (var output = new FileOutputStream("private-jwk.key.json")) {
      output.write(jwk.toJSONString().getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Generates an encryption key for config "encryption.secret-key" value.
   *
   * <p>Output file: {root}/private-encryption.key
   */
  @Test
  @Disabled("Not actually a test. Enable when needed.")
  @SneakyThrows
  void generateEncryptionKey() {
    javax.crypto.KeyGenerator generator;
    try {
      generator = javax.crypto.KeyGenerator.getInstance("AES");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    generator.init(256);
    var key = Base64.getEncoder().encode(generator.generateKey().getEncoded());

    try (var output = new FileOutputStream("private-encryption.key")) {
      output.write(key);
    }
  }
}
