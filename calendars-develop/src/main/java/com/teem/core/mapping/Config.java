package com.UoU.core.mapping;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapping configuration suitable for most uses. Use @Mapper props to override anything here.
 */
@MapperConfig(
    componentModel = "spring", // generate spring @Component for DI
    injectionStrategy = InjectionStrategy.CONSTRUCTOR, // use ctor injection like our own code
    unmappedTargetPolicy = ReportingPolicy.ERROR // fail build to force explicitly ignoring props
)
public interface Config {
}
