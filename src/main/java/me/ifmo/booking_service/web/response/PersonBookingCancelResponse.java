package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Person cancellation result. Tickets are not modified.")
public record PersonBookingCancelResponse(
        @Schema(description = "Person whose bookings were cancelled.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long personId,
        @Schema(description = "Number of deleted bookings; zero if none existed.", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedBookingsCount
) {
}
