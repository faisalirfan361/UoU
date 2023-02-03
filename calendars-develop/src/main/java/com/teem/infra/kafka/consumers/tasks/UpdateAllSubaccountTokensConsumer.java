package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.nylas.tasks.UpdateAllSubaccountTokensTask;
import com.UoU.infra.avro.tasks.UpdateAllSubaccountTokens;
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
    ConfigPaths.Tasks.UPDATE_ALL_SUBACCOUNT_TOKENS + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class UpdateAllSubaccountTokensConsumer {
  private static final String CONF = ConfigPaths.Tasks.UPDATE_ALL_SUBACCOUNT_TOKENS;
  private static final Runner RUNNER = new Runner(
      log, UpdateAllSubaccountTokensConsumer.class.getSimpleName());

  private final UpdateAllSubaccountTokensTask task;

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
  private void consume(ConsumerRecord<String, UpdateAllSubaccountTokens> record) {
    RUNNER.runWithRetry(retry -> {
      val serviceAccountId = new ServiceAccountId(UUID.fromString(
          record.value().getServiceAccountId()));
      val params = new UpdateAllSubaccountTokensTask.Params(serviceAccountId);

      retry.run(() -> task.run(params));
    });
  }
}
