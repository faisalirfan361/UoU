package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.tasks.AdvanceEventsActivePeriodTask;
import com.UoU.core.tasks.UpdateExpiredServiceAccountRefreshTokensTask;
import com.UoU.core.tasks.UpdateServiceAccountRefreshTokenTask;
import com.UoU.infra.avro.tasks.Maintenance;
import com.UoU.infra.avro.tasks.MaintenanceObjectType;
import com.UoU.infra.kafka.ConfigPaths;
import com.UoU.infra.kafka.NoRetryException;
import com.UoU.infra.kafka.consumers.Runner;
import java.util.Optional;
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

/**
 * Consumer for general system maintenance tasks.
 */
@Component
@ConditionalOnProperty({
    "kafka.consumers-enabled",
    ConfigPaths.Tasks.MAINTENANCE + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class MaintenanceConsumer {
  private static final String CONF = ConfigPaths.Tasks.MAINTENANCE;
  private static final Runner RUNNER = new Runner(log, MaintenanceConsumer.class.getSimpleName());

  private final AdvanceEventsActivePeriodTask advanceEventsActivePeriodTask;
  private final UpdateExpiredServiceAccountRefreshTokensTask
      updateExpiredServiceAccountRefreshTokensTask;
  private final UpdateServiceAccountRefreshTokenTask updateServiceAccountRefreshTokenTask;

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
  private void consume(ConsumerRecord<String, Maintenance> record) {
    RUNNER.runWithRetry(() -> record.value().getAction().toString(), retry -> {
      // Dispatch to particular maintenance task via action enum:
      val action = record.value().getAction();
      switch (action) {
        case ADVANCE_EVENTS_ACTIVE_PERIOD -> retry
            .run(advanceEventsActivePeriodTask::run);

        case UPDATE_EXPIRED_SERVICE_ACCOUNT_REFRESH_TOKENS -> retry
            .run(updateExpiredServiceAccountRefreshTokensTask::run);

        case UPDATE_SERVICE_ACCOUNT_REFRESH_TOKEN -> {
          val params = new UpdateServiceAccountRefreshTokenTask.Params(getServiceAccountId(record));
          retry.run(() -> updateServiceAccountRefreshTokenTask.run(params));
        }

        default -> throw new IllegalArgumentException("Invalid maintenance action: " + action);
      }
    });
  }

  private static ServiceAccountId getServiceAccountId(ConsumerRecord<String, Maintenance> record) {
    return record.value()
        .getObject()
        .filter(obj -> MaintenanceObjectType.SERVICE_ACCOUNT.equals(obj.getType()))
        .flatMap(obj -> {
          try {
            return Optional.of(UUID.fromString(obj.getId()));
          } catch (IllegalArgumentException ex) {
            return Optional.empty();
          }
        })
        .map(ServiceAccountId::new)
        .orElseThrow(() -> new IllegalArgumentException(
            "Record does not contain a valid service account id for action: "
                + record.value().getAction()));
  }
}
