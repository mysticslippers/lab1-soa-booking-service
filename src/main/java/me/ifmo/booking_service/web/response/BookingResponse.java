package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сохранённое бронирование билета пользователем.")
public record BookingResponse(
        @Schema(description = "Идентификатор бронирования, сформированный сервером.", example = "1", minimum = "1", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Идентификатор пользователя.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long personId,
        @Schema(description = "Идентификатор билета.", example = "105", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long ticketId,
        @Schema(description = "Время создания в UTC, сформированное сервером.", example = "2026-10-07T12:00:00Z", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant createdAt
) {
}
