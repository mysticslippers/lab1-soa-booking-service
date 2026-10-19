package me.ifmo.booking_service.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Единый формат ответа об ошибке. Внутренние исключения и трассировки стека не раскрываются.")
public record ApiErrorResponse(
        @Schema(description = "Код состояния HTTP-ответа.", example = "422", minimum = "400", maximum = "599", requiredMode = Schema.RequiredMode.REQUIRED)
        int status,
        @Schema(description = "Машинный код ошибки.", example = "VALIDATION_ERROR", requiredMode = Schema.RequiredMode.REQUIRED)
        String error,
        @Schema(description = "Текстовое описание ошибки на английском языке.", example = "Invalid request body fields", requiredMode = Schema.RequiredMode.REQUIRED)
        String message,
        @Schema(description = "Ошибки проверки полей; пустой объект, если сведения об отдельных полях отсутствуют.", example = "{\"personId\":\"must be greater than 0\"}", requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> details,
        @Schema(description = "Время возникновения ошибки в UTC; см. полные примеры ответов.", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant timestamp
) {
}
