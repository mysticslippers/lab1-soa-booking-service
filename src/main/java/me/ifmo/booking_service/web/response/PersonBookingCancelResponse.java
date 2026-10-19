package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат отмены бронирований пользователя. Билеты не изменяются.")
public record PersonBookingCancelResponse(
        @Schema(description = "Пользователь, бронирования которого были отменены.", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long personId,
        @Schema(description = "Количество удалённых бронирований; ноль, если бронирований не было.", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long deletedBookingsCount
) {
}
