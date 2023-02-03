package com.UoU.app.security;

import com.UoU.core.nylas.ClientConfig;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.val;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

@Service
public class HmacUtil {
  private static final String HMAC_ALG = "HmacSHA256";
  private final Mac hmac = Mac.getInstance(HMAC_ALG);

  public HmacUtil(ClientConfig config) throws NoSuchAlgorithmException, InvalidKeyException {
    hmac.init(
        new SecretKeySpec(config.secret().value().getBytes(StandardCharsets.UTF_8), HMAC_ALG));
  }

  public boolean validate(String hmac, String message) {
    val expectedHmac = createHmac(message);
    return hmac.equals(expectedHmac);
  }

  public String createHmac(String message) {
    return new String(Hex.encode(hmac.doFinal(message.getBytes(StandardCharsets.UTF_8))));
  }
}
