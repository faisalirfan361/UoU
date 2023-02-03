package com.UoU.infra.kafka;

import static com.UoU.infra.kafka.ConfigPaths.PublicEvents;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class NewTopics {

  @Bean
  @ConditionalOnProperty(PublicEvents.EVENT_CHANGED + ".topic.create.enabled")
  public NewTopic topicPublicEventChanged(
      @Value("${" + PublicEvents.EVENT_CHANGED + ".topic.name}") String name,
      @Value("${" + PublicEvents.EVENT_CHANGED + ".topic.create.partitions}") int partitions,
      @Value("${" + PublicEvents.EVENT_CHANGED + ".topic.create.replication}") int replication) {
    return TopicBuilder.name(name).partitions(partitions).replicas(replication).build();
  }
}
