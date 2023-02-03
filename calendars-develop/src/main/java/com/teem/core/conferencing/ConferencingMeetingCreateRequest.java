package com.UoU.core.conferencing;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request to create a new conferencing meeting.
 *
 * @param principalEmail The principal creating the event, which should come from API auth. This is
 *                       needed to verify authorized access to the supplied userId.
 * @param userId         The userId for auth, which also determines the conferencing provider.
 * @param language       The optional language to request from the provider, such as en-US.
 */
public record ConferencingMeetingCreateRequest(
    @NotNull @Email String principalEmail,
    @NotNull @Valid ConferencingUserId userId,
    @Size(max = ConferencingConstraints.LANGUAGE_MAX) String language
) {
}
