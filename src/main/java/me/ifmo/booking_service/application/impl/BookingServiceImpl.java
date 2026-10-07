package me.ifmo.booking_service.application.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.booking_service.application.BookingService;
import me.ifmo.booking_service.client.TicketServiceClient;
import me.ifmo.booking_service.client.response.TicketsByEventDeleteResponse;
import me.ifmo.booking_service.domain.Booking;
import me.ifmo.booking_service.mappers.BookingMapper;
import me.ifmo.booking_service.persistence.BookingRepository;
import me.ifmo.booking_service.web.error.exceptions.ResourceConflictException;
import me.ifmo.booking_service.web.error.exceptions.ResourceNotFoundException;
import me.ifmo.booking_service.web.request.BookingCreateRequest;
import me.ifmo.booking_service.web.response.BookingResponse;
import me.ifmo.booking_service.web.response.EventBookingCancelResponse;
import me.ifmo.booking_service.web.response.PersonBookingCancelResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final TicketServiceClient ticketServiceClient;

    @Override
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        ticketServiceClient.requireTicket(request.ticketId());

        if (repository.existsByPersonIdAndTicketId(request.personId(), request.ticketId()))
            throw new ResourceConflictException("Booking for person with id '%s' and ticket with id '%s' already exists".formatted(request.personId(), request.ticketId()));

        Booking booking = mapper.toEntity(request);
        Booking saved = repository.save(booking);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        Booking booking = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Booking with id '%s' not found".formatted(id)));

        return mapper.toResponse(booking);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Booking existing = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Booking with id '%s' not found".formatted(id)));

        repository.delete(existing);
    }

    @Override
    @Transactional
    public PersonBookingCancelResponse cancelByPerson(Long personId) {
        long deletedBookingsCount = repository.deleteByPersonId(personId);
        return new PersonBookingCancelResponse(personId, deletedBookingsCount);
    }

    @Override
    @Transactional
    public EventBookingCancelResponse cancelByEvent(Integer eventId) {
        TicketsByEventDeleteResponse response = ticketServiceClient.deleteByEvent(eventId);
        long deletedBookingsCount = response.ticketIds().isEmpty() ? 0 : repository.deleteByTicketIdIn(response.ticketIds());

        return new EventBookingCancelResponse(eventId, response.count(), deletedBookingsCount);
    }
}
