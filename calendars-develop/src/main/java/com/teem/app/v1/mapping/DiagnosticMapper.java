package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.DiagnosticEventDto;
import com.UoU.app.v1.dtos.DiagnosticRequestDto;
import com.UoU.app.v1.dtos.DiagnosticResultsDto;
import com.UoU.core.OrgId;
import com.UoU.core.diagnostics.DiagnosticRequest;
import com.UoU.core.diagnostics.Results;
import com.UoU.core.diagnostics.events.DiagnosticEvent;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface DiagnosticMapper {
  Pattern DIAGNOSTIC_EVENT_NAME_PATTERN = Pattern.compile("([a-z0-9])([A-Z])");
  String DIAGNOSTIC_EVENT_NAME_REPLACE = "$1-$2";

  DiagnosticRequest toModel(DiagnosticRequestDto request, OrgId orgId);

  @Mapping(target = "calendarId", source = "runId.calendarId")
  @Mapping(target = "runId", source = "runId.id")
  @Mapping(target = "durationSeconds", expression = "java(toDurationSeconds(results))")
  DiagnosticResultsDto toDto(Results results);

  @Mapping(target = "name", expression = "java(toEventName(event))")
  @Mapping(target = "isError", source = "event.error")
  DiagnosticEventDto toDto(DiagnosticEvent event);

  default String toEventName(DiagnosticEvent event) {
    return event == null ? null : DIAGNOSTIC_EVENT_NAME_PATTERN
        .matcher(event.getClass().getSimpleName())
        .replaceAll(DIAGNOSTIC_EVENT_NAME_REPLACE)
        .toLowerCase(Locale.ROOT);
  }

  default Long toDurationSeconds(Results results) {
    return Optional
        .ofNullable(results)
        .flatMap(x -> x.duration())
        .map(x -> x.toSeconds())
        .orElse(null);
  }
}
