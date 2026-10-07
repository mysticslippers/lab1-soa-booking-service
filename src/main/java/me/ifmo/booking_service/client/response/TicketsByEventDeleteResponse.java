package me.ifmo.booking_service.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketsByEventDeleteResponse(
        Integer eventId,
        List<Long> ticketIds,
        Long count
) {
}
