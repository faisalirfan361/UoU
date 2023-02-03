package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public enum NylasActionDto {
  @JsonProperty("event.created") EVENT_CREATED("event.created"),
  @JsonProperty("event.updated") EVENT_UPDATED("event.updated"),
  @JsonProperty("event.deleted") EVENT_DELETED("event.deleted"),
  @JsonProperty("calendar.created") CALENDAR_CREATED("calendar.created"),
  @JsonProperty("calendar.updated") CALENDAR_UPDATED("calendar.updated"),
  @JsonProperty("calendar.deleted") CALENDAR_DELETED("calendar.deleted"),
  @JsonProperty("account.running") ACCOUNT_RUNNING("account.running"),
  @JsonProperty("account.stopped") ACCOUNT_STOPPED("account.stopped"),
  @JsonProperty("account.connected") ACCOUNT_CONNECTED("account.connected"),
  @JsonProperty("account.invalid") ACCOUNT_INVALID("account.invalid");

  @Getter
  private final String value;

  NylasActionDto(String value) {
    this.value = value;
  }
}
