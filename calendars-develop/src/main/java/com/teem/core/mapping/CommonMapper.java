package com.UoU.core.mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;

/**
 * Mapper for basic, common Java types, to be re-used by other mappers to avoid duplication.
 *
 * <p>Use as needed in other mappers with something like:
 * <pre>{@code
 * @Mapper(config = Config.class, uses = CommonMapper.class)
 * }</pre>
 */
@Mapper(config = Config.class)
public interface CommonMapper {
  default OffsetDateTime mapToOffsetDateTime(Instant value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }

  default Instant mapToInstant(OffsetDateTime value) {
    return value == null ? null : value.toInstant();
  }
}
