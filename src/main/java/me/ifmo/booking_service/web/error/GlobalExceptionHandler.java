package me.ifmo.booking_service.web.error;

import lombok.extern.slf4j.Slf4j;
import me.ifmo.booking_service.web.error.exceptions.ResourceConflictException;
import me.ifmo.booking_service.web.error.exceptions.ResourceNotFoundException;
import me.ifmo.booking_service.web.error.exceptions.TicketServiceException;
import me.ifmo.booking_service.web.response.ApiErrorResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final HttpStatusCode UNPROCESSABLE_CONTENT = HttpStatusCode.valueOf(422);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return respond(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException exception) {
        return respond(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(TicketServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleTicketService(TicketServiceException exception) {
        log.warn("Ticket Service request failed", exception);
        HttpStatusCode status = exception.getStatusCode();
        String message = exception.getReason() != null ? exception.getReason() : "Ticket Service request failed";
        return respond(status, errorCode(exception, status), message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause())
            if (cause instanceof ConstraintViolationException violation && "unique_bookings_person_ticket".equals(violation.getConstraintName()))
                return respond(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", "Booking for this person and ticket already exists");

        return handleUnexpected(exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected request failure", exception);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        HttpHeaders errorHeaders = new HttpHeaders();
        errorHeaders.putAll(headers);
        errorHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (exception instanceof MethodArgumentNotValidException validationException) {
            boolean requestBody = validationException.getParameter().hasParameterAnnotation(RequestBody.class);
            HttpStatusCode validationStatus = requestBody ? UNPROCESSABLE_CONTENT : HttpStatus.BAD_REQUEST;
            String code = requestBody ? "VALIDATION_ERROR" : "INVALID_PARAMETER";
            String message = requestBody ? "Invalid request body fields" : "Invalid request parameters";

            return super.handleExceptionInternal(exception,
                    error(validationStatus, code, message, validationDetails(validationException.getBindingResult())),
                    errorHeaders, validationStatus, request);
        }

        if (exception instanceof HandlerMethodValidationException validationException && !validationException.isForReturnValue()) {
            boolean requestBodyOnly = !validationException.getParameterValidationResults().isEmpty()
                    && validationException.getCrossParameterValidationResults().isEmpty()
                    && validationException.getParameterValidationResults().stream()
                    .allMatch(result -> result.getMethodParameter().hasParameterAnnotation(RequestBody.class));
            HttpStatusCode validationStatus = requestBodyOnly ? UNPROCESSABLE_CONTENT : HttpStatus.BAD_REQUEST;
            String code = requestBodyOnly ? "VALIDATION_ERROR" : "INVALID_PARAMETER";
            String message = requestBodyOnly ? "Invalid request body fields" : "Invalid request parameters";

            return super.handleExceptionInternal(exception,
                    error(validationStatus, code, message, methodValidationDetails(validationException)),
                    errorHeaders, validationStatus, request);
        }

        if (status.is5xxServerError())
            log.error("Request processing failed", exception);

        return super.handleExceptionInternal(exception,
                error(status, errorCode(exception, status), errorMessage(exception, status), Map.of()),
                errorHeaders, status, request);
    }

    private static Map<String, String> validationDetails(Errors errors) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : errors.getFieldErrors())
            details.merge(fieldError.getField(), messageOrDefault(fieldError), (first, next) -> first + "; " + next);

        for (ObjectError objectError : errors.getGlobalErrors())
            details.merge("_object", messageOrDefault(objectError), (first, next) -> first + "; " + next);

        return details;
    }

    private static Map<String, String> methodValidationDetails(HandlerMethodValidationException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            if (result instanceof ParameterErrors errors)
                validationDetails(errors).forEach((field, message) ->
                        details.merge(field, message, (first, next) -> first + "; " + next));
            else {
                String parameter = result.getMethodParameter().getParameterName();
                String key = parameter != null ? parameter : "parameter";
                for (MessageSourceResolvable error : result.getResolvableErrors())
                    details.merge(key, messageOrDefault(error), (first, next) -> first + "; " + next);
            }
        }

        for (MessageSourceResolvable error : exception.getCrossParameterValidationResults())
            details.merge("_request", messageOrDefault(error), (first, next) -> first + "; " + next);

        return details;
    }

    private static String messageOrDefault(MessageSourceResolvable error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
    }

    private static String errorCode(Exception exception, HttpStatusCode status) {
        if (exception instanceof HttpMessageNotReadableException)
            return "INVALID_REQUEST_BODY";

        if (exception instanceof TypeMismatchException || exception instanceof MissingServletRequestParameterException)
            return "INVALID_PARAMETER";

        return switch (status.value()) {
            case 400 -> "INVALID_PARAMETER";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 409 -> "RESOURCE_CONFLICT";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            case 422 -> "VALIDATION_ERROR";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> status.is5xxServerError() ? "INTERNAL_ERROR" : "INVALID_REQUEST";
        };
    }

    private static String errorMessage(Exception exception, HttpStatusCode status) {
        if (status.is5xxServerError())
            return "Internal server error";

        if (exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null && !responseStatusException.getReason().isBlank())
            return responseStatusException.getReason();

        if (exception instanceof HttpMessageNotReadableException)
            return "Malformed or missing request body";

        return switch (status.value()) {
            case 400 -> "Invalid request parameters";
            case 404 -> "Resource not found";
            case 405 -> "HTTP method not allowed";
            case 406 -> "Cannot produce the requested response format";
            case 409 -> "Resource state conflict";
            case 415 -> "Unsupported request content type";
            case 422 -> "Invalid request data";
            default -> "Request processing error";
        };
    }

    private static ResponseEntity<ApiErrorResponse> respond(HttpStatusCode status, String code, String message) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(error(status, code, message, Map.of()));
    }

    private static ApiErrorResponse error(HttpStatusCode status, String code, String message, Map<String, String> details) {
        return new ApiErrorResponse(status.value(), code, message, details, Instant.now());
    }
}
