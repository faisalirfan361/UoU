package com.UoU.infra.kafka.consumers.tasks;

import static com.UoU.infra.avro.tasks.DiagnosticsAction.RUN_CALENDAR_SYNC_DIAGNOSTICS;

import com.UoU.core.calendars.CalendarId;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.tasks.CalendarSyncDiagnosticTask;
import com.UoU.infra.avro.tasks.Diagnostics;
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
    ConfigPaths.Tasks.DIAGNOSTICS + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class DiagnosticsConsumer {
  private static final String CONF = ConfigPaths.Tasks.DIAGNOSTICS;
  private static final Runner RUNNER = new Runner(log, DiagnosticsConsumer.class.getSimpleName());

  private final CalendarSyncDiagnosticTask calendarSyncDiagnosticTask;

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
          NoRetryException.class, IllegalArgumentException.class, IllegalStateException.class})
  private void consume(ConsumerRecord<String, Diagnostics> record) {
    RUNNER.runWithRetry(retry -> {
      // Only RUN_CALENDAR_SYNC_DIAGNOSTICS is currently supported:
      if (record.value().getAction() != RUN_CALENDAR_SYNC_DIAGNOSTICS) {
        throw new IllegalArgumentException(
            "Invalid diagnostics action: " + record.value().getAction());
      }

      val runId = new RunId(
          new CalendarId(record.value().getCalendarId()),
          UUID.fromString(record.value().getRunId()));
      val params = record.value().getCallbackUri()
          .map(uri -> new CalendarSyncDiagnosticTask.Params(runId, uri))
          .orElseGet(() -> new CalendarSyncDiagnosticTask.Params(runId));

      retry.run(() -> calendarSyncDiagnosticTask.run(params));
    });
  }
}
