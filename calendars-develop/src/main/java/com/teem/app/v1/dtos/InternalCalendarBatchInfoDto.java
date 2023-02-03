package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;

@Schema(name = "InternalCalendarBatchInfo",
    requiredProperties = SchemaExt.Required.ALL,
    example = """
        {
          "1": {
            "id": "68fc50b1-0876-45c8-bc86-fb4ffdc46436",
            "name": "Desk 3.1",
            "email": "68fc50b1-0876-45c8-bc86-fb4ffdc46436-calendar@example.com"
          },
          "2": {
            "id": "ce17d8e6-9e4a-43df-8621-92b255123192",
            "name": "Desk 3.2",
            "email": "ce17d8e6-9e4a-43df-8621-92b255123192-calendar@example.com"
          },
          "3": {
            "id": "6a464dfa-0234-4981-8865-6db395b8949a",
            "name": "Desk 3.3",
            "email": "6a464dfa-0234-4981-8865-6db395b8949a"
          }
        }
        """)
public class InternalCalendarBatchInfoDto extends LinkedHashMap<Integer, InternalCalendarInfoDto> {
}
