package me.ifmo.booking_service.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Данные для создания бронирования. id и createdAt формируются сервером.")
public record BookingCreateRequest(
        @NotNull
        @Positive
        @Schema(description = "Идентификатор пользователя; пара пользователь–билет должна быть уникальной.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long personId,

        @NotNull
        @Positive
        @Schema(description = "Идентификатор существующего билета, проверяемый через Ticket Service.", example = "105", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long ticketId
) {
}
