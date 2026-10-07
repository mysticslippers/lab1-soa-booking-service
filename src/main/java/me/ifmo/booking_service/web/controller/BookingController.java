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
@Tag(name = "Bookings")
@RequiredArgsConstructor
@RequestMapping(value = "/booking", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses({
        @ApiResponse(responseCode = "405", description = "HTTP method not allowed; supported methods are listed in Allow",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "406", description = "Requested response format is not supported",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
})
public class BookingController {

    private final BookingService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "201", description = "Booking created", useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI of the created booking",
                    schema = @Schema(type = "string", format = "uri")))
    @Operation(summary = "Create a booking", description = "Verifies that the ticket exists in Ticket Service. The personId and ticketId pair must be unique.", responses = {
            @ApiResponse(responseCode = "400", description = "Malformed or missing JSON body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Booking for this person and ticket already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "415", description = "Unsupported request content type",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "personId or ticketId is missing or is not positive",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Ticket Service returned an invalid response or an unexpected HTTP status",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Ticket Service is unavailable",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "504", description = "Ticket Service request timed out",
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
    @ApiResponse(responseCode = "200", description = "Booking found", useReturnTypeSchema = true)
    @Operation(summary = "Get a booking by id", responses = {
            @ApiResponse(responseCode = "400", description = "Booking id must be a positive integer within the int64 range",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public BookingResponse getById(@Parameter(description = "Booking id", example = "1") @PathVariable @Positive Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "Booking deleted; the ticket remains unchanged", content = @Content)
    @Operation(summary = "Delete a booking", responses = {
            @ApiResponse(responseCode = "400", description = "Booking id must be a positive integer within the int64 range",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void delete(@Parameter(description = "Booking id", example = "1") @PathVariable @Positive Long id) {
        service.delete(id);
    }

    @DeleteMapping("/person/{personId}/cancel")
    @ApiResponse(responseCode = "200", description = "Deleted booking count", useReturnTypeSchema = true)
    @Operation(summary = "Cancel a person's bookings", description = "Deletes only bookings. Tickets remain unchanged. A person with no bookings returns a zero count.", responses = {
            @ApiResponse(responseCode = "400", description = "Person id must be a positive integer within the int64 range",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public PersonBookingCancelResponse cancelByPerson(@Parameter(description = "Person id", example = "1") @PathVariable @Positive Long personId) {
        return service.cancelByPerson(personId);
    }

    @DeleteMapping("/event/{eventId}/cancel")
    @ApiResponse(responseCode = "200", description = "Deleted ticket and booking counts", useReturnTypeSchema = true)
    @Operation(summary = "Cancel an event's tickets and bookings", description = "Deletes tickets through Ticket Service, then deletes their bookings locally. The event object is preserved. An existing event with no tickets returns zero counts. Deletion across the two services is not atomic.", responses = {
            @ApiResponse(responseCode = "400", description = "Event id must be a positive integer within the int32 range",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Ticket Service returned an invalid response or an unexpected HTTP status",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Ticket Service is unavailable",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "504", description = "Ticket Service request timed out",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public EventBookingCancelResponse cancelByEvent(@Parameter(description = "Event id", example = "15") @PathVariable @Positive Integer eventId) {
        return service.cancelByEvent(eventId);
    }
}
