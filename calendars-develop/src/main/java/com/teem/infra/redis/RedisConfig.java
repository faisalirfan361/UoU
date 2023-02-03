package com.UoU.infra.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.UoU.core.Fluent;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
class RedisConfig {

  @Bean
  RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    val jsonSerializer = Fluent.of(new ObjectMapper())
        .also(x -> x.registerModule(new JavaTimeModule()))
        .also(x -> x.activateDefaultTyping(
            x.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY))
        .map(GenericJackson2JsonRedisSerializer::new)
        .get();

    return Fluent.of(new RedisTemplate<String, Object>())
        .also(x -> x.setConnectionFactory(redisConnectionFactory))
        .also(x -> x.setDefaultSerializer(jsonSerializer))
        .also(x -> x.setKeySerializer(RedisSerializer.string()))
        .also(x -> x.setHashKeySerializer(RedisSerializer.string()))
        .get();
  }
}
