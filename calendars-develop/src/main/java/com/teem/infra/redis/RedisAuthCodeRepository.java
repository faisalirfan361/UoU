package com.UoU.infra.redis;

import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthCode;
import com.UoU.core.auth.AuthCodeCreateRequest;
import com.UoU.core.auth.AuthCodeRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Repository that stores auto-expiring auth codes in redis.
 */
@Service
@AllArgsConstructor
@Slf4j
public class RedisAuthCodeRepository implements AuthCodeRepository {
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Optional<AuthCode> tryGet(UUID code) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(Key.create(code)))
        .map(x -> (Value) x)
        .map(x -> new AuthCode(code, new OrgId(x.getOrgId()), x.getRedirectUri()));
  }

  @Override
  public void create(AuthCodeCreateRequest request) {
    if (request.expiration().toSeconds() <= 0) {
      return; // expiration has passed, so there's nothing to do.
    }

    redisTemplate.opsForValue().set(
        Key.create(request.code()),
        new Value(request.orgId().value(), request.redirectUri()),
        request.expiration());

    log.debug("Auth code saved to redis: {}, expires in {}", request.code(), request.expiration());
  }

  @Override
  public void tryDelete(UUID code) {
    redisTemplate.delete(Key.create(code));
    log.debug("Auth code deleted from redis: {}", code);
  }

  /**
   * The key to store in redis.
   */
  private static class Key {
    private static final String PREFIX = "auth-code-";

    public static String create(UUID code) {
      return PREFIX + code;
    }
  }

  /**
   * The value to store in redis.
   */
  @AllArgsConstructor
  @Getter
  private static class Value {
    private final String orgId;
    private final String redirectUri;
  }
}
