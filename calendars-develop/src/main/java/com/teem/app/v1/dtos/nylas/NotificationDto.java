package com.UoU.app.v1.dtos.nylas;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.UoU.app.v1.dtos.NylasActionDto;
import java.util.List;

public record NotificationDto(
    List<Delta> deltas
) {
  public record Delta(
      long date,
      String object,
      NylasActionDto type,
      @JsonProperty("object_data")
      ObjectData objectData
  ) {

    public record ObjectData(
        @JsonProperty("account_id")String accountId,
        String object,
        String id
    ) {
    }
  }
}
