package me.ifmo.booking_service.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import me.ifmo.booking_service.web.response.ApiErrorResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingServiceOpenApi(
            @Value("${OPENAPI_SERVER_URL:${spring.mvc.servlet.path}}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title("Booking Service API").version("1.0.0")
                        .description("Stores person-ticket bookings separately from Ticket Service. Event cancellation calls Ticket Service over HTTP and then deletes local bookings; the two deletions are not atomic. Request bodies and responses use JSON. Body validation errors return 422; malformed bodies and invalid URL parameters return 400."))
                .servers(List.of(new Server().url(serverUrl).description("API base URL; paths below do not repeat /api")))
                .tags(List.of(
                        new Tag().name("Bookings").description("Booking creation, lookup, deletion and cancellation by person or event.")
                ));
    }

    @Bean
    public OpenApiCustomizer bookingServiceDocumentation(ObjectMapper mapper) {
        Map<String, Object> examples = Map.ofEntries(
                Map.entry("BookingCreateRequest", mapper.readValue("{\"personId\":1,\"ticketId\":105}", Object.class)),
                Map.entry("BookingResponse", mapper.readValue("{\"id\":1,\"personId\":1,\"ticketId\":105,\"createdAt\":\"2026-10-07T12:00:00Z\"}", Object.class)),
                Map.entry("PersonBookingCancelResponse", mapper.readValue("{\"personId\":1,\"deletedBookingsCount\":3}", Object.class)),
                Map.entry("EventBookingCancelResponse", mapper.readValue("{\"eventId\":15,\"deletedTicketsCount\":2,\"deletedBookingsCount\":3}", Object.class))
        );

        return api -> {
            if (api.getComponents() == null) api.setComponents(new Components());
            ModelConverters.getInstance(true).read(ApiErrorResponse.class).forEach((name, schema) -> {
                if (api.getComponents().getSchemas() == null || !api.getComponents().getSchemas().containsKey(name))
                    api.getComponents().addSchemas(name, schema);
            });
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                operation.getResponses().putIfAbsent("405", new ApiResponse()
                        .description("HTTP method is not allowed; supported methods are listed in the Allow header"));
                if (!path.equals("/"))
                    operation.getResponses().putIfAbsent("406", new ApiResponse().description("Requested response format is not supported"));
                operation.getResponses().putIfAbsent("500", new ApiResponse().description("Unexpected server error"));

                operation.getResponses().forEach((code, response) -> {
                    if (code.startsWith("4") || code.startsWith("5")) {
                        int status = Integer.parseInt(code);
                        response.setContent(new Content().addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
                                .example(errorExample(status, path, method.name()))));
                        if (code.equals("405")) {
                            String allowed = String.join(", ", item.readOperationsMap().keySet().stream()
                                    .flatMap(allowedMethod -> allowedMethod.name().equals("GET")
                                            ? java.util.stream.Stream.of("GET", "HEAD") : java.util.stream.Stream.of(allowedMethod.name()))
                                    .toList());
                            response.addHeaderObject("Allow", new Header().description("Supported HTTP methods")
                                    .schema(new StringSchema()).example(allowed));
                        }
                    } else if (response.getContent() != null) {
                        response.getContent().values().forEach(media -> addExample(media, examples));
                    }
                });
                if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null)
                    operation.getRequestBody().getContent().values().forEach(media -> addExample(media, examples));
            }));
        };
    }

    private static void addExample(MediaType media, Map<String, Object> examples) {
        Schema<?> schema = media.getSchema();
        if (schema == null) return;
        boolean array = schema.getItems() != null;
        String reference = array ? schema.getItems().get$ref() : schema.get$ref();
        if (reference == null) return;
        Object example = examples.get(reference.substring(reference.lastIndexOf('/') + 1));
        if (example != null && media.getExample() == null && (media.getExamples() == null || media.getExamples().isEmpty()))
            media.setExample(array ? List.of(example) : example);
    }

    private static Map<String, Object> errorExample(int status, String path, String method) {
        String code = switch (status) {
            case 400 -> method.equals("POST") || method.equals("PUT") ? "INVALID_REQUEST_BODY" : "INVALID_PARAMETER";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 409 -> "RESOURCE_CONFLICT";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            case 422 -> "VALIDATION_ERROR";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> "INTERNAL_ERROR";
        };
        String message = switch (status) {
            case 400 -> code.equals("INVALID_REQUEST_BODY") ? "Malformed or missing request body" : "Invalid request parameters";
            case 404 -> path.startsWith("/coordinates") ? "Coordinates with id '1' not found"
                    : path.startsWith("/events") || path.contains("/event/") || path.contains("by-event/")
                    ? "Event with id '15' not found" : path.startsWith("/booking") && !method.equals("POST")
                    ? "Booking with id '1' not found" : "Ticket with id '105' not found";
            case 405 -> "HTTP method not allowed";
            case 406 -> "Cannot produce the requested response format";
            case 409 -> path.startsWith("/coordinates") ? "Coordinates with id '1' cannot be deleted because tickets reference it"
                    : path.startsWith("/events") ? "Event with id '15' cannot be deleted because tickets reference it"
                    : "Booking for person with id '1' and ticket with id '105' already exists";
            case 415 -> "Unsupported request content type";
            case 422 -> "Invalid request body fields";
            case 502 -> "Ticket Service returned an invalid response";
            case 503 -> "Ticket Service is unavailable";
            case 504 -> "Ticket Service request timed out";
            default -> "Internal server error";
        };
        Map<String, String> details = status == 422
                ? path.startsWith("/coordinates") ? Map.of("x", "must not be null")
                : path.startsWith("/booking") ? Map.of("personId", "must be greater than 0")
                : Map.of("name", "must not be empty") : Map.of();
        return Map.of("status", status, "error", code, "message", message,
                "details", details, "timestamp", "2026-10-07T12:00:00Z");
    }
}
