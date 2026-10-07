package me.ifmo.booking_service.application;

import me.ifmo.booking_service.web.request.BookingCreateRequest;
import me.ifmo.booking_service.web.response.BookingResponse;
import me.ifmo.booking_service.web.response.EventBookingCancelResponse;
import me.ifmo.booking_service.web.response.PersonBookingCancelResponse;

public interface BookingService {
    BookingResponse create(BookingCreateRequest request);

    BookingResponse getById(Long id);

    void delete(Long id);

    PersonBookingCancelResponse cancelByPerson(Long personId);

    EventBookingCancelResponse cancelByEvent(Integer eventId);
}
