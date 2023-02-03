package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.Fluent;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import com.UoU.core.nylas.tasks.DeleteEventFromNylasTask;
import com.UoU.core.nylas.tasks.ExportEventToNylasTask;
import com.UoU.core.nylas.tasks.HandleEventDeleteFromNylasTask;
import com.UoU.core.nylas.tasks.ImportEventFromNylasTask;
import com.UoU.infra.avro.tasks.ChangeEvent;
import com.UoU.infra.kafka.ConfigPaths;
import com.UoU.infra.kafka.NoRetryException;
import com.UoU.infra.kafka.consumers.Runner;
import java.util.UUID;
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

@Component
@ConditionalOnProperty({
    "kafka.consumers-enabled",
    ConfigPaths.Tasks.CHANGE_EVENT + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class ChangeEventConsumer {
  private static final String CONF = ConfigPaths.Tasks.CHANGE_EVENT;
  private static final Runner RUNNER = new Runner(log, ChangeEventConsumer.class.getSimpleName());

  private final ImportEventFromNylasTask importEventFromNylasTask;
  private final HandleEventDeleteFromNylasTask handleEventDeleteFromNylasTask;
  private final ExportEventToNylasTask exportEventToNylasTask;
  private final DeleteEventFromNylasTask deleteEventFromNylasTask;

  // TODO: For all consumers, analyze the exceptions and make sure we're only retrying when it
  // would actually help. For example, this task throw an exception for an invalid change action,
  // but retrying would never help. I think we need retry/noretry logic at the core task level,
  // which could then be translated to the kafka NoRetryException.
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
      exclude = {
          NoRetryException.class, IllegalArgumentException.class, NotFoundException.class,
          ReadOnlyException.class
      })
  private void consume(ConsumerRecord<String, ChangeEvent> record) {
    RUNNER.runWithRetry(() -> record.value().getAction().toString(), retry -> {
      val action = record.value().getAction();
      val accountId = new AccountId(record.value().getAccountId());

      switch (action) {
        case IMPORT_FROM_NYLAS -> retry.run(() -> Fluent
            .of(new ImportEventFromNylasTask.Params(accountId, getExternalId(record)))
            .also(importEventFromNylasTask::run));

        case DELETE -> retry.run(() -> Fluent
            .of(new HandleEventDeleteFromNylasTask.Params(accountId, getExternalId(record)))
            .also(handleEventDeleteFromNylasTask::run));

        case EXPORT_TO_NYLAS -> retry.run(() -> Fluent
            .of(new ExportEventToNylasTask.Params(accountId, getEventId(record)))
            .also(exportEventToNylasTask::run));

        case DELETE_FROM_NYLAS -> retry.run(() -> Fluent
            .of(new DeleteEventFromNylasTask.Params(accountId, getExternalId(record)))
            .also(deleteEventFromNylasTask::run));

        default -> throw new IllegalArgumentException("Invalid action: " + action);
      }
    });
  }

  private static EventId getEventId(ConsumerRecord<String, ChangeEvent> record) {
    return record.value().getEventId()
        .map(x -> new EventId(UUID.fromString(x)))
        .orElseThrow(() -> new IllegalArgumentException(
            "Action " + record.value().getAction() + " requires eventId"));
  }

  private static EventExternalId getExternalId(ConsumerRecord<String, ChangeEvent> record) {
    return record.value().getExternalId()
        .map(EventExternalId::new)
        .orElseThrow(() -> new IllegalArgumentException(
            "Action " + record.value().getAction() + " requires externalId"));
  }
}
