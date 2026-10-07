package me.ifmo.booking_service.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketLookupResponse(
        Long id
) {
}
