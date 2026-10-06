package me.ifmo.booking_service.persistence;

import me.ifmo.booking_service.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByPersonIdAndTicketId(Long personId, Long ticketId);

    long deleteByPersonId(Long personId);

    long deleteByTicketIdIn(Collection<Long> ticketIds);
}
