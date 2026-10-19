package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат отмены билетов и бронирований события. Само событие сохраняется.")
public record EventBookingCancelResponse(
        @Schema(description = "Событие, билеты которого были отменены.", example = "15", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer eventId,
        @Schema(description = "Количество билетов, удалённых через Ticket Service.", example = "2", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedTicketsCount,
        @Schema(description = "Количество бронирований, удалённых в локальной базе; может отличаться от количества билетов.", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedBookingsCount
) {
}
