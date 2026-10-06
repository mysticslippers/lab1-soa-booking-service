package me.ifmo.booking_service.web.response;

public record PersonBookingCancelResponse(
        Long personId,
        long deletedBookingsCount
) {
}
