package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Stored person-ticket booking.")
public record BookingResponse(
        @Schema(description = "Server-generated booking id.", example = "1", minimum = "1", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Person id.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long personId,
        @Schema(description = "Ticket id.", example = "105", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long ticketId,
        @Schema(description = "Server-generated creation time in UTC.", example = "2026-10-07T12:00:00Z", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant createdAt
) {
}
