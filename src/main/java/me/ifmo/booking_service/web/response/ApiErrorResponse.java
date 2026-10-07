package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Uniform error response. Internal exceptions and stack traces are never exposed.")
public record ApiErrorResponse(
        @Schema(description = "HTTP response status.", example = "422", minimum = "400", maximum = "599", requiredMode = Schema.RequiredMode.REQUIRED)
        int status,
        @Schema(description = "Machine-readable error code.", example = "VALIDATION_ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
        String error,
        @Schema(description = "Human-readable English error description.", example = "Invalid request body fields", requiredMode = Schema.RequiredMode.REQUIRED)
        String message,
        @Schema(description = "Field validation errors; an empty object for errors without field details.", example = "{\"personId\":\"must be greater than 0\"}", requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> details,
        @Schema(description = "UTC time when the error occurred; see the complete response examples.", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant timestamp
) {
}
