package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.nylas.tasks.DeleteAccountFromNylasTask;
import com.UoU.infra.avro.tasks.DeleteAccountFromNylas;
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
    ConfigPaths.Tasks.DELETE_ACCOUNT_FROM_NYLAS + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class DeleteAccountFromNylasConsumer {
  private static final String CONF = ConfigPaths.Tasks.DELETE_ACCOUNT_FROM_NYLAS;
  private static final Runner RUNNER = new Runner(
      log, DeleteAccountFromNylasConsumer.class.getSimpleName());

  private final DeleteAccountFromNylasTask task;

  // DO-MAYBE: We may be able to use meta-annotations or global config to remove a lot of this
  // boilerplate code. See https://docs.spring.io/spring-kafka/reference/html/#kafka-listener-meta
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
  private void consume(ConsumerRecord<String, DeleteAccountFromNylas> record) {
    RUNNER.runWithRetry(retry -> {
      val params = new DeleteAccountFromNylasTask.Params(
          new AccountId(record.value().getAccountId()));

      retry.run(() -> task.run(params));
    });
  }
}
