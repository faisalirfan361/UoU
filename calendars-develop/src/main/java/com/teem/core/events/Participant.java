package com.UoU.core.events;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public record Participant(
    String name,
    @NotEmpty @Email String email,
    ParticipantStatus status,
    String comment
) {
}
