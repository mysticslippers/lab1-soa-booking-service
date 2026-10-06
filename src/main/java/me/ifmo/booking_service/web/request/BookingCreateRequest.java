package me.ifmo.booking_service.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingCreateRequest(
        @NotNull
        @Positive
        Long personId,

        @NotNull
        @Positive
        Long ticketId
) {
}
