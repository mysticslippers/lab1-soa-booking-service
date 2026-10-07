package me.ifmo.booking_service.client;

import lombok.RequiredArgsConstructor;
import me.ifmo.booking_service.client.response.TicketLookupResponse;
import me.ifmo.booking_service.client.response.TicketsByEventDeleteResponse;
import me.ifmo.booking_service.web.error.exceptions.ResourceNotFoundException;
import me.ifmo.booking_service.web.error.exceptions.TicketServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.HashSet;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TicketServiceClient {

    private final RestClient ticketServiceRestClient;

    public void require(Long ticketId) {
        TicketLookupResponse response = handleRequest(() -> ticketServiceRestClient.get().uri("/tickets/{id}", ticketId)
                        .retrieve().toEntity(TicketLookupResponse.class), "Ticket with id '%s' not found".formatted(ticketId));

        if (!ticketId.equals(response.id()))
            throw new TicketServiceException(HttpStatus.BAD_GATEWAY, "Ticket Service returned an invalid response");
    }

    public TicketsByEventDeleteResponse deleteByEvent(Integer eventId) {
        TicketsByEventDeleteResponse response = handleRequest(() -> ticketServiceRestClient.delete().uri("/tickets/by-event/{eventId}", eventId)
                        .retrieve().toEntity(TicketsByEventDeleteResponse.class), "Event with id '%s' not found".formatted(eventId));

        if (!eventId.equals(response.eventId()) || response.ticketIds() == null
                || response.count() == null || response.count() < 0 || response.count() != response.ticketIds().size()
                || response.ticketIds().stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(response.ticketIds()).size() != response.ticketIds().size())
            throw new TicketServiceException(HttpStatus.BAD_GATEWAY, "Ticket Service returned an invalid response");

        return response;
    }

    private <T> T handleRequest(Supplier<ResponseEntity<T>> action, String notFoundMessage) {
        try {
            ResponseEntity<T> response = action.get();
            if (response.getStatusCode().value() != 200 || response.getBody() == null)
                throw new TicketServiceException(HttpStatus.BAD_GATEWAY, "Ticket Service returned an invalid response");

            return response.getBody();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404)
                throw new ResourceNotFoundException(notFoundMessage);

            if (exception.getStatusCode().is5xxServerError())
                throw new TicketServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Ticket Service is unavailable", exception);

            throw new TicketServiceException(HttpStatus.BAD_GATEWAY, "Ticket Service returned an unexpected HTTP status", exception);
        } catch (RestClientException exception) {
            if (exception.contains(SocketTimeoutException.class) || exception.contains(HttpTimeoutException.class))
                throw new TicketServiceException(HttpStatus.GATEWAY_TIMEOUT, "Ticket Service request timed out", exception);

            if (exception instanceof ResourceAccessException)
                throw new TicketServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Ticket Service is unavailable", exception);

            throw new TicketServiceException(HttpStatus.BAD_GATEWAY, "Ticket Service returned an invalid response", exception);
        }
    }
}
