package com.UoU.infra.kafka.consumers.events;

import com.UoU.core.events.EventId;
import com.UoU.core.events.EventRepository;
import com.UoU.infra.avro.events.EventChanged;
import com.UoU.infra.kafka.ConfigPaths;
import com.UoU.infra.kafka.NoRetryException;
import com.UoU.infra.kafka.PublicEventProducer;
import com.UoU.infra.kafka.consumers.Runner;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Consumer for internal EventChanged event, which enriches and produces the public EventChanged.
 */
@Component
@ConditionalOnProperty({
    "kafka.consumers-enabled",
    ConfigPaths.Events.EVENT_CHANGED + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class EventChangedConsumer {
  private static final String CONF = ConfigPaths.Events.EVENT_CHANGED;
  private static final Runner RUNNER = new Runner(log, EventChangedConsumer.class.getSimpleName());
  private final EventRepository eventRepo;
  private final PublicEventProducer publicEventProducer;

  @KafkaListener(topics = {"${" + CONF + ".topic.name}"})
  @RetryableTopic(
      autoCreateTopics = "${" + CONF + ".topic.create.enabled}",
      numPartitions = "${" + CONF + ".topic.create.partitions}",
      replicationFactor = "${" + CONF + ".topic.create.replication}",
      retryTopicSuffix = "--${spring.kafka.consumer.group-id}.retry",
      dltTopicSuffix = "--${spring.kafka.consumer.group-id}.dlt",
      topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
      attempts = "${" + CONF + ".retry.attempts}",
      backoff = @Backoff(
          delayExpression = "${" + CONF + ".retry.backoff.delay}",
          multiplierExpression = "${" + CONF + ".retry.backoff.multiplier}"),
      exclude = {NoRetryException.class, IllegalArgumentException.class})
  private void consume(ConsumerRecord<String, EventChanged> record) {
    RUNNER.runWithRetry(() -> record.value().getChangeType().toString(), retry -> {
      val changeType = record.value().getChangeType();

      // Avro has an issue where it creates the ids as List<Utf8> instead of List<String>, which
      // causes cast errors, so cast list to List<?> first and use toString() on items.
      val ids = ((List<?>) record.value().getEventIds())
          .stream()
          .map(x -> new EventId(UUID.fromString(x.toString())))
          .toList();

      retry.run(() -> {
        val eventBatch = eventRepo.listById(ids).toList();

        switch (changeType) {
          case created -> publicEventProducer.eventCreated(eventBatch);
          case updated -> publicEventProducer.eventUpdated(eventBatch);
          default -> throw new IllegalArgumentException("Invalid change type: " + changeType);
        }

        // If some ids could not be found, something weird happened, but retrying won't help and
        // there's probably nothing to do. Just log a warning in case some later fix is needed.
        if (eventBatch.size() != ids.size() && log.isWarnEnabled()) {
          val foundIds = eventBatch.stream()
              .map(x -> x.id())
              .collect(Collectors.toSet());
          val missingIds = ids.stream()
              .filter(x -> !foundIds.contains(x))
              .map(x -> x.value().toString())
              .collect(Collectors.joining(", "));
          log.warn("Failed producing PUBLIC EventChanged ({}) for missing events: {}", missingIds);
        }
      });
    });
  }
}
