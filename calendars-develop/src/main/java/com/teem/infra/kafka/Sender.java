package com.UoU.infra.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Wraps KafkaTemplate.send() to enable/disable sending based on our custom config.
 */
@Service
@Slf4j
class Sender {
  boolean enabled;
  KafkaTemplate<String, Object> kafkaTemplate;

  public Sender(
      @Value("${kafka.producers-enabled:true}") boolean enabled,
      KafkaTemplate<String, Object> kafkaTemplate) {

    this.enabled = enabled;
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topic, String key, Object value) {
    if (enabled) {
      kafkaTemplate.send(topic, key, value);
    } else {
      log.debug("Kafka producer disabled for: topic={}, key={}", topic, key);
    }
  }

  public void send(String topic, Object value) {
    if (enabled) {
      kafkaTemplate.send(topic, value);
    } else {
      log.debug("Kafka producer disabled for: topic={}", topic);
    }
  }
}
