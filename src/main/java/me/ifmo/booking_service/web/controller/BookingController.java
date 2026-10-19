package me.ifmo.booking_service.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.ifmo.booking_service.application.BookingService;
import me.ifmo.booking_service.web.request.BookingCreateRequest;
import me.ifmo.booking_service.web.response.ApiErrorResponse;
import me.ifmo.booking_service.web.response.BookingResponse;
import me.ifmo.booking_service.web.response.EventBookingCancelResponse;
import me.ifmo.booking_service.web.response.PersonBookingCancelResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Tag(name = "Бронирования")
@RequiredArgsConstructor
@RequestMapping(value = "/booking", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses({
        @ApiResponse(responseCode = "405", description = "Метод HTTP не поддерживается; допустимые методы перечислены в заголовке Allow",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "406", description = "Запрошенный формат ответа не поддерживается",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
})
public class BookingController {

    private final BookingService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "Бронирование создано", useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI созданного бронирования",
                    schema = @Schema(type = "string", format = "uri")))
    @Operation(summary = "Создать бронирование", description = "Проверяет существование билета в Ticket Service. Пара personId и ticketId должна быть уникальной.", responses = {
            @ApiResponse(responseCode = "400", description = "Тело JSON отсутствует или имеет некорректный формат",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Билет не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Бронирование для этого пользователя и билета уже существует",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "415", description = "Неподдерживаемый тип содержимого запроса",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "personId или ticketId отсутствует либо не является положительным числом",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Сервис билетов вернул некорректный ответ или неожиданный код состояния HTTP",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Сервис билетов недоступен",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "504", description = "Истекло время ожидания ответа сервиса билетов",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = BookingCreateRequest.class),
            examples = @ExampleObject(value = "{\"personId\": 1, \"ticketId\": 105}")))
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingCreateRequest request) {
        BookingResponse response = service.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Бронирование найдено", useReturnTypeSchema = true)
    @Operation(summary = "Получить бронирование по идентификатору", description = "Возвращает сохранённое бронирование без получения или изменения билета.", responses = {
            @ApiResponse(responseCode = "400", description = "Идентификатор бронирования должен быть положительным целым числом в диапазоне int64",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public BookingResponse getById(@Parameter(description = "Идентификатор бронирования", example = "1", schema = @Schema(minimum = "1")) @PathVariable @Positive Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "Бронирование удалено; билет не изменён", content = @Content)
    @Operation(summary = "Удалить бронирование", description = "Удаляет только бронирование. Билет не изменяется.", responses = {
            @ApiResponse(responseCode = "400", description = "Идентификатор бронирования должен быть положительным целым числом в диапазоне int64",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Идентификатор бронирования", example = "1", schema = @Schema(minimum = "1")) @PathVariable @Positive Long id) {
        service.delete(id);
    }

    @DeleteMapping("/person/{personId}/cancel")
    @ApiResponse(responseCode = "200", description = "Количество удалённых бронирований", useReturnTypeSchema = true)
    @Operation(summary = "Отменить бронирования пользователя", description = "Удаляет только бронирования. Билеты не изменяются. Если у пользователя нет бронирований, возвращается нулевое количество.", responses = {
            @ApiResponse(responseCode = "400", description = "Идентификатор пользователя должен быть положительным целым числом в диапазоне int64",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public PersonBookingCancelResponse cancelByPerson(@Parameter(description = "Идентификатор пользователя", example = "1", schema = @Schema(minimum = "1")) @PathVariable @Positive Long personId) {
        return service.cancelByPerson(personId);
    }

    @DeleteMapping("/event/{eventId}/cancel")
    @ApiResponse(responseCode = "200", description = "Количество удалённых билетов и бронирований", useReturnTypeSchema = true)
    @Operation(summary = "Отменить билеты и бронирования события", description = "Удаляет билеты через Ticket Service, затем удаляет их бронирования в локальной базе. Само событие сохраняется. Для существующего события без билетов возвращаются нулевые счётчики. Удаление в двух сервисах не является атомарной операцией.", responses = {
            @ApiResponse(responseCode = "400", description = "Идентификатор события должен быть положительным целым числом в диапазоне int32",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Событие не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Сервис билетов вернул некорректный ответ или неожиданный код состояния HTTP",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Сервис билетов недоступен",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "504", description = "Истекло время ожидания ответа сервиса билетов",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public EventBookingCancelResponse cancelByEvent(@Parameter(description = "Идентификатор события", example = "15", schema = @Schema(minimum = "1")) @PathVariable @Positive Integer eventId) {
        return service.cancelByEvent(eventId);
    }
}
