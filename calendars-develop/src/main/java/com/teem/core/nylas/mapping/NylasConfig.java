package com.UoU.core.nylas.mapping;

import com.UoU.core.mapping.WrappedValueMapper;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration for Nylas mappers. Use @Mapper props to override anything here.
 */
@MapperConfig(
    componentModel = "spring", // generate spring @Component for DI
    injectionStrategy = InjectionStrategy.CONSTRUCTOR, // use ctor injection like our own code
    unmappedTargetPolicy = ReportingPolicy.IGNORE, // Nylas records will have many unmapped props
    collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE,
    uses = WrappedValueMapper.class
)

public interface NylasConfig {
}

