package me.ifmo.booking_service.mappers;

import me.ifmo.booking_service.domain.Booking;
import me.ifmo.booking_service.web.request.BookingCreateRequest;
import me.ifmo.booking_service.web.response.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(BookingCreateRequest request);

    BookingResponse toResponse(Booking booking);
}
