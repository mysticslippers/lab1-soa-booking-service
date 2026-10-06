package me.ifmo.booking_service.web.response;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long personId,
        Long ticketId,
        Instant createdAt
) {
}
