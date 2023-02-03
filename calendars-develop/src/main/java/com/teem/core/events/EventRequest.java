package com.UoU.core.events;

import com.UoU.core.OrgId;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request params for an event operation, such as a checkin or delete.
 *
 * <p>Note that some operations will require more params and therefore a more specific request
 * object, such as {@link EventCreateRequest} or {@link EventUpdateRequest}. This type holds the
 * generic params necessary for more basic operations.
 */
public record EventRequest(
    @Valid @NotNull EventId id,
    @Valid @NotNull OrgId orgId,
    @Valid DataSource dataSource
) {
}
