package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.calendars.CalendarId;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.tasks.ExportCalendarsToNylasTask;
import com.UoU.infra.avro.tasks.ExportCalendarsToNylas;
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
    ConfigPaths.Tasks.EXPORT_CALENDARS_TO_NYLAS + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class ExportCalendarsToNylasConsumer {
  private static final String CONF = ConfigPaths.Tasks.EXPORT_CALENDARS_TO_NYLAS;
  private static final Runner RUNNER = new Runner(
      log, ExportCalendarsToNylasConsumer.class.getSimpleName());

  private final ExportCalendarsToNylasTask task;

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
          IllegalOperationException.class, IllegalStateException.class
      })
  private void consume(ConsumerRecord<String, ExportCalendarsToNylas> record) {
    RUNNER.runWithRetry(retry -> {
      val params = new ExportCalendarsToNylasTask.Params(
          record.value().getIds().stream().map(CalendarId::new).toList(),
          record.value().getIncludeEvents());

      retry.run(() -> task.run(params));
    });
  }
}
