package com.UoU.infra.redis.scripts;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * RedisScript wrapper for lua script (see lua file for implementation).
 */
@Component
public class LockWithCountScript extends DefaultRedisScript<Boolean> {
  protected LockWithCountScript() {
    setResultType(Boolean.class);
    setLocation(new ClassPathResource("redis/lock-with-count.lua"));
  }
}
