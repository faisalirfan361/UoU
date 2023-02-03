package com.UoU.app.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.util.Base64;
import java.security.InvalidKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtDecoderTests {
  private static final String JWT_AUDIENCE = "test";
  private static final String JWT = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwib3JnX"
      + "2lkIjoidGVzdCIsImF1ZCI6InRlc3QiLCJpc3MiOiJmb3ItdGVzdGluZyIsImlhdCI6MTY0NDYxMzUzMSwiZXhwIjo"
      + "zMjUwMzcwNTIwMCwic2NvcGUiOiJhY2NvdW50cyBjYWxlbmRhcnMgZXZlbnRzIGRpYWdub3N0aWNzIn0.C-UmmDCEE"
      + "_6uPZKnbVWn549D3UAuFEQGHhbmaDWNcqvEHgaMgyy6x2bIM1VXlNUu5nI1wamM9H0AqwPE3WDPCCo95w3pmNdNuPD"
      + "J1LK2j9I0HlcTHrS6N52-qxp7H_Ke5FY_18YzQKXqRuaS-h4NQnIKCssopYYXmzuM9hvfEPpHdbICu1CL-kGomXsaI"
      + "_x0PNWCB0HAFMtzFWh1J2aI8tkNNV345MsPG2mZw1hu7OqV96E4C8t5M-qTbPExWreNHR2zsOllBgsSAO1heX1hem7"
      + "LwSMdNuBDa7Jd62k8ZS5Hocu4oFP9-cq5KLUr2-qI3pnENqN4PsxdKjiVB8F1hw";
  private static final String JWK_PUBLIC_KEY = "{"
      + "\"kty\": \"RSA\","
      + "\"e\":\"AQAB\","
      + "\"n\":\"o-6IfE-hozVvUpid0938r8P9ecSDCxGZBaL2XcT2to5awQxln7ZYIqo01vSW59GDMCJq2X5j7kfP81zSg"
      + "dZMWD8WgFOYupzlGyQ69okOJ1a5wA-6Rm7zKT8rqhqCsRgPCoqD5rlq--8ZwOVctO_mStSff2-yyk1dPKcMzl5dPq3"
      + "Y6BS-pB_4578ua6NZ40rVzo5auCHpwpQqK_JwzTTkID4I4oPRe9BliVM1I_-r1PKWfbJxUL3KQKAS26vKUxTz5OlaX"
      + "R1VellmWOV_o58sYKlmhRfnxSqBeuu6VBH6wrwxlgqxtNvPs4FLXIy9dMVJMw_EUWCXaxcu3FN7v-yPIQ\""
      + "}";

  private JwtValidator validatorMock;

  @BeforeEach
  void setUp() {
    validatorMock = mock(JwtValidator.class);
    when(validatorMock.validate(any(Jwt.class))).thenReturn(OAuth2TokenValidatorResult.success());
  }

  @Test
  void ctor_shouldFailWithInvalidKey() {
    var config = new JwtConfig(JWT_AUDIENCE, "invalid-key");

    assertThatCode(() -> new JwtDecoder(config, validatorMock))
        .isInstanceOf(InvalidKeyException.class);
  }

  @Test
  void decode_shouldFailWithWrongKey() {
    var wrongKey = "{"
        + "\"kty\":\"RSA\","
        + "\"e\":\"AQAB\","
        + "\"n\":\"olE5oJxnTjo9UzCa6_OA6pwQf8WNZYWFeg8Qv9TDG37XaMSYDY_Q_zl6TPnTrH4_SgWONhwxYERmhyqT"
        + "2o5sMJBX3ftKrC5wqfFzmO9O_MhkyW4QbqvwgytakzmNcGXrTk9gZHUTUjZvXhs1k9qYTfYhs8vGkPtH3KiciNdE"
        + "YSMWSOtL2payq4NcdWcuAYQCilYOERfhUv9pXYzXwEmLrEJ1lVa3EWwuNZw559YfCVakWT6lec3bMq0oHahyxpKA"
        + "zsdNJi3nUERwheTiYFxwTvp-7oZ1Aqg92Ouoy1MgN5sJ3L8iMGwMyb13z__N9SZ0nUdq5d_tJcRm8C9hM0PT1w\""
        + "}";
    var config = new JwtConfig(JWT_AUDIENCE, wrongKey);

    var decoder = new JwtDecoder(config, validatorMock);
    assertThatCode(() -> decoder.decode(JWT))
        .isInstanceOf(BadJwtException.class);
  }

  @Test
  void decode_shouldUseJwkToDecodeAndValidate() {
    var config = new JwtConfig(JWT_AUDIENCE, JWK_PUBLIC_KEY);
    var decoder = new JwtDecoder(config, validatorMock);
    var result = decoder.decode(JWT);

    assertThat(result.getSubject()).isNotEmpty();
    verify(validatorMock).validate(any(Jwt.class));
  }

  @Test
  void decode_shouldUseBase64JwkToDecodeAndValidate() {
    var config = new JwtConfig(JWT_AUDIENCE, Base64.encode(JWK_PUBLIC_KEY).toString());
    var decoder = new JwtDecoder(config, validatorMock);
    var result = decoder.decode(JWT);

    assertThat(result.getSubject()).isNotEmpty();
    verify(validatorMock).validate(any(Jwt.class));
  }
}
