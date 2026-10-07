package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Event cancellation result. The event itself is preserved.")
public record EventBookingCancelResponse(
        @Schema(description = "Event whose tickets were cancelled.", example = "15", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer eventId,
        @Schema(description = "Number of tickets deleted by Ticket Service.", example = "2", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedTicketsCount,
        @Schema(description = "Number of locally deleted bookings; can differ from the ticket count.", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedBookingsCount
) {
}
