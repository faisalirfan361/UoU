package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.Fluent;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.tasks.HandleCalendarDeleteFromNylasTask;
import com.UoU.core.nylas.tasks.ImportCalendarFromNylasTask;
import com.UoU.infra.avro.tasks.ChangeCalendar;
import com.UoU.infra.kafka.ConfigPaths;
import com.UoU.infra.kafka.NoRetryException;
import com.UoU.infra.kafka.consumers.Runner;
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
    ConfigPaths.Tasks.CHANGE_CALENDAR + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class ChangeCalendarConsumer {
  private static final String CONF = ConfigPaths.Tasks.CHANGE_CALENDAR;
  private static final Runner RUNNER = new Runner(
      log, ChangeCalendarConsumer.class.getSimpleName());

  private final ImportCalendarFromNylasTask importCalendarFromNylasTask;
  private final HandleCalendarDeleteFromNylasTask handleCalendarDeleteFromNylasTask;

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
      exclude = {NoRetryException.class, IllegalArgumentException.class, NotFoundException.class})
  private void consume(ConsumerRecord<String, ChangeCalendar> record) {
    RUNNER.runWithRetry(() -> record.value().getAction().toString(), retry -> {
      val action = record.value().getAction();
      val accountId = new AccountId(record.value().getAccountId());
      val calendarExternalId = new CalendarExternalId(record.value().getCalendarExternalId());

      switch (action) {
        case IMPORT_FROM_NYLAS -> Fluent
            .of(new ImportCalendarFromNylasTask.Params(accountId, calendarExternalId, false))
            .also(importCalendarFromNylasTask::run);

        case IMPORT_FROM_NYLAS_WITH_EVENTS -> Fluent
            .of(new ImportCalendarFromNylasTask.Params(accountId, calendarExternalId, true))
            .also(importCalendarFromNylasTask::run);

        case DELETE -> Fluent
            .of(new HandleCalendarDeleteFromNylasTask.Params(accountId, calendarExternalId))
            .also(handleCalendarDeleteFromNylasTask::run);

        default -> throw new IllegalArgumentException("Invalid action: " + action);
      }
    });
  }
}
