package com.UoU.infra.db.mapping;

import com.UoU.core.mapping.WrappedValueMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration for jooq mappers. Use @Mapper props to override anything here.
 */
@MapperConfig(
    componentModel = "spring", // generate spring @Component for DI
    injectionStrategy = InjectionStrategy.CONSTRUCTOR, // use ctor injection like our own code
    unmappedTargetPolicy = ReportingPolicy.IGNORE, // jooq records will have many unmapped props
    uses = WrappedValueMapper.class
)
public interface JooqConfig {
}
