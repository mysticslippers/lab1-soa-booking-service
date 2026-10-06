package me.ifmo.booking_service.web.response;

public record EventBookingCancelResponse(
        Integer eventId,
        long deletedTicketsCount,
        long deletedBookingsCount
) {
}
