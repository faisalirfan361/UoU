package com.UoU.infra.kafka;

import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.tasks.TaskScheduler;
import com.UoU.infra.avro.tasks.Maintenance;
import com.UoU.infra.avro.tasks.MaintenanceAction;
import com.UoU.infra.avro.tasks.MaintenanceObject;
import com.UoU.infra.avro.tasks.MaintenanceObjectType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
class TaskProducer implements TaskScheduler {
  private final Sender sender;
  private final TopicNames.Tasks topicNames;

  @Override
  public void advanceEventsActivePeriod() {
    sender.send(
        topicNames.getMaintenance(),
        null,
        Maintenance.newBuilder()
            .setAction(MaintenanceAction.ADVANCE_EVENTS_ACTIVE_PERIOD)
            .build());
  }

  @Override
  public void updateExpiredServiceAccountRefreshTokens() {
    sender.send(
        topicNames.getMaintenance(),
        null,
        Maintenance.newBuilder()
            .setAction(MaintenanceAction.UPDATE_EXPIRED_SERVICE_ACCOUNT_REFRESH_TOKENS)
            .build());
  }

  @Override
  public void updateServiceAccountRefreshToken(ServiceAccountId id) {
    sender.send(
        topicNames.getMaintenance(),
        null,
        Maintenance.newBuilder()
            .setAction(MaintenanceAction.UPDATE_SERVICE_ACCOUNT_REFRESH_TOKEN)
            .setObject(MaintenanceObject.newBuilder()
                .setType(MaintenanceObjectType.SERVICE_ACCOUNT)
                .setId(id.value().toString())
                .build())
            .build());
  }
}
