package com.UoU.infra.kafka.consumers.tasks;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.auth.NylasAuthException;
import com.UoU.core.nylas.tasks.UpdateSubaccountTokenTask;
import com.UoU.infra.avro.tasks.UpdateSubaccountToken;
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
  ConfigPaths.Tasks.UPDATE_SUBACCOUNT_TOKEN + ".consumer-enabled"})
@AllArgsConstructor
@Slf4j
public class UpdateSubaccountTokenConsumer {
  private static final String CONF = ConfigPaths.Tasks.UPDATE_SUBACCOUNT_TOKEN;
  private static final Runner RUNNER = new Runner(
      log, UpdateSubaccountTokenConsumer.class.getSimpleName());

  private final UpdateSubaccountTokenTask task;

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
          NylasAuthException.class
      })
  private void consume(ConsumerRecord<String, UpdateSubaccountToken> record) {
    RUNNER.runWithRetry(retry -> {
      val serviceAccountId = new ServiceAccountId(UUID.fromString(
          record.value().getServiceAccountId()));
      val accountId = new AccountId(record.value().getAccountId());
      val params = new UpdateSubaccountTokenTask.Params(serviceAccountId, accountId);

      retry.run(() -> task.run(params));
    });
  }
}
