package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public enum ProviderDto {
  @JsonProperty("microsoft") MICROSOFT("microsoft"),
  @JsonProperty("google") GOOGLE("google");

  @Getter
  private final String value;

  ProviderDto(String value) {
    this.value = value;
  }
}
