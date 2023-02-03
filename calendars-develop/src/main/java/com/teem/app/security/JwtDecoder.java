package com.UoU.app.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

/**
 * Wraps the NimbusJwtDecoder and configures it to use the public key from JwtConfig.
 */
@Service
class JwtDecoder implements org.springframework.security.oauth2.jwt.JwtDecoder {
  private final NimbusJwtDecoder decoder;

  @SneakyThrows
  public JwtDecoder(@NonNull JwtConfig config, @NonNull JwtValidator validator) {
    decoder = NimbusJwtDecoder.withPublicKey(getPublicKey(config.publicJwk())).build();
    decoder.setJwtValidator(validator);
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    return decoder.decode(token);
  }

  @SneakyThrows
  private static RSAPublicKey getPublicKey(String jwk) {
    try {
      // If jwk is not json, assume it's base64 encoded.
      // Support base64 so that it's easier to store in an env variable.
      var json = jwk.contains("{")
          ? jwk
          : new String(Base64.getDecoder().decode(jwk), StandardCharsets.UTF_8);

      return JWK.parse(json).toRSAKey().toRSAPublicKey();
    } catch (IllegalArgumentException | ParseException | JOSEException ex) {
      throw new InvalidKeyException("Invalid jwk public key in config");
    }
  }
}
