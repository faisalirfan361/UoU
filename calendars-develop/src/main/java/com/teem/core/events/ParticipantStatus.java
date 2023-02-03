package com.UoU.core.events;

import lombok.Getter;

public enum ParticipantStatus {
  NO_REPLY("noreply"),
  YES("yes"),
  NO("no"),
  MAYBE("maybe");

  @Getter
  public final String value;

  ParticipantStatus(String value) {
    this.value = value;
  }
}
