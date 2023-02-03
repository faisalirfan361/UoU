package com.UoU.infra.redis;

import com.UoU.core.events.EventExternalId;
import com.UoU.core.nylas.EtagConfig;
import com.UoU.core.nylas.ExternalEtag;
import com.UoU.core.nylas.ExternalEtagRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisExternalEtagRepository implements ExternalEtagRepository {
  private final EtagConfig config;
  private final StringRedisTemplate redisTemplate;

  @Override
  public Optional<ExternalEtag> get(EventExternalId externalId) {
    return Optional
        .ofNullable(redisTemplate.opsForValue().get(Key.create(externalId)))
        .map(ExternalEtag::new);
  }

  @Override
  public Map<EventExternalId, ExternalEtag> get(Set<EventExternalId> externalIds) {
    val idsInOrder = new ArrayList<EventExternalId>();
    val keys = new ArrayList<String>();
    externalIds.forEach(id -> {
      idsInOrder.add(id);
      keys.add(Key.create(id));
    });

    val results = redisTemplate.opsForValue().multiGet(keys);

    // Result & key size and indexes should match, but ensure redis adapter never changes behavior:
    if (results.size() != keys.size()) {
      throw new IndexOutOfBoundsException("Redis multiGet result is not expected size.");
    }

    val map = new HashMap<EventExternalId, ExternalEtag>();
    for (var i = 0; i < results.size(); i++) {
      val value = results.get(i);
      if (value != null && !value.isEmpty()) {
        map.put(idsInOrder.get(i), new ExternalEtag(value));
      }
    }

    return map;
  }

  @Override
  public void save(EventExternalId externalId, ExternalEtag etag) {
    redisTemplate.opsForValue().set(Key.create(externalId), etag.toString(), config.expiration());
  }

  @Override
  public void save(Map<EventExternalId, ExternalEtag> etags) {
    val keysAndValues = etags.entrySet()
        .stream()
        .collect(Collectors.toMap(x -> Key.create(x.getKey()), x -> x.getValue().toString()));
    val expireSecs = config.expiration().toSeconds();

    // redisTemplate multiSet doesn't take expirations, so we'll use a pipeline with setex.
    redisTemplate.executePipelined((RedisCallback<?>) conn -> {
      val stringConn = (StringRedisConnection) conn;
      keysAndValues.entrySet().forEach(x -> stringConn.setEx(x.getKey(), expireSecs, x.getValue()));
      return null; // RedisCallback requires return
    });
  }

  @Override
  public void tryDelete(EventExternalId externalId) {
    redisTemplate.delete(Key.create(externalId));
  }

  @Override
  public void tryDelete(Set<EventExternalId> externalIds) {
    val keys = externalIds.stream().map(Key::create).toList();
    redisTemplate.delete(keys);
  }

  private static class Key {
    private static final String PREFIX = "event-external-etag-";

    public static String create(EventExternalId externalId) {
      return PREFIX + externalId.value();
    }
  }
}
