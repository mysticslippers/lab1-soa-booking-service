package me.ifmo.booking_service.web.response;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> details,
        Instant timestamp
) {
}
