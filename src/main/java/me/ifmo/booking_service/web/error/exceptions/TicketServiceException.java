package me.ifmo.booking_service.web.error.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TicketServiceException extends ResponseStatusException {
    public TicketServiceException(HttpStatus status, String message) {
        super(status, message);
    }

    public TicketServiceException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
