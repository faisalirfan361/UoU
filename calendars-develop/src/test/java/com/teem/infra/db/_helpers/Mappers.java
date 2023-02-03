package com.UoU.infra.db._helpers;

import com.UoU.core.mapping.WrappedValueMapper;
import com.UoU.core.mapping.WrappedValueMapperImpl;
import com.UoU.infra.db.mapping.JooqEventMapper;
import com.UoU.infra.db.mapping.JooqEventMapperImpl;
import com.UoU.infra.db.mapping.JooqParticipantMapper;
import com.UoU.infra.db.mapping.JooqParticipantMapperImpl;

public class Mappers {
  public static final WrappedValueMapper WRAPPED_VALUE_MAPPER = new WrappedValueMapperImpl();

  public static final JooqParticipantMapper PARTICIPANT_MAPPER = new JooqParticipantMapperImpl(
      WRAPPED_VALUE_MAPPER);

  public static final JooqEventMapper EVENT_MAPPER = new JooqEventMapperImpl(
      PARTICIPANT_MAPPER, WRAPPED_VALUE_MAPPER);
}
