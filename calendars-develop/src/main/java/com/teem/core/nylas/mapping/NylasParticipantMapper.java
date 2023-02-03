package com.UoU.core.nylas.mapping;

import com.UoU.core.events.Participant;
import com.UoU.core.events.ParticipantStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(config = NylasConfig.class)
public interface NylasParticipantMapper {
  Participant toParticipantModel(com.nylas.Participant participant);

  @ValueMapping(target = "YES", source = "yes")
  @ValueMapping(target = "NO", source = "no")
  @ValueMapping(target = "MAYBE", source = "maybe")
  @ValueMapping(target = "NO_REPLY", source = "noreply")
  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_UNMAPPED)
  ParticipantStatus mapStatus(String status);
}
