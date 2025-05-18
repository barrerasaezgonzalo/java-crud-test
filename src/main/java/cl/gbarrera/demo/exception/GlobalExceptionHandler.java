package cl.gbarrera.demo.exception;

import cl.gbarrera.demo.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static cl.gbarrera.demo.util.Messages.MALFORMED_JSON_OR_FIELD_TYPES;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import cl.gbarrera.demo.exception.InvalidProductException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            InvalidProductException ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        ErrorResponse errorResponse =
                new ErrorResponse(
                        ex.getStatus().value(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        requestId,
                        LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();

        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        MALFORMED_JSON_OR_FIELD_TYPES,
                        request.getRequestURI(),
                        requestId,
                        LocalDateTime.now()
                );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String requestId = UUID.randomUUID().toString();

        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        MALFORMED_JSON_OR_FIELD_TYPES,
                        request.getRequestURI(),
                        requestId,
                        LocalDateTime.now()
                );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<InvalidProductException> handleUnrecognizedField(UnrecognizedPropertyException ex) {
        InvalidProductException error = new InvalidProductException(
                MALFORMED_JSON_OR_FIELD_TYPES,
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(error, error.getStatus());
    }
}
