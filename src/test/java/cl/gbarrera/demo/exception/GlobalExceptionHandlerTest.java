package cl.gbarrera.demo.exception;

import cl.gbarrera.demo.model.ErrorResponse;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Objects;

import static cl.gbarrera.demo.util.Messages.MALFORMED_JSON_OR_FIELD_TYPES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleInvalidProductException_shouldReturnExpectedResponse() {
        InvalidProductException ex = new InvalidProductException("Producto inválido", HttpStatus.NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleProductNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Producto inválido", Objects.requireNonNull(response.getBody()).getError());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleValidationException_shouldReturnBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MALFORMED_JSON_OR_FIELD_TYPES, Objects.requireNonNull(response.getBody()).getError());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleMessageNotReadableException_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadableException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MALFORMED_JSON_OR_FIELD_TYPES, Objects.requireNonNull(response.getBody()).getError());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleUnrecognizedField_shouldReturnBadRequest() {
        UnrecognizedPropertyException ex = mock(UnrecognizedPropertyException.class);

        ResponseEntity<InvalidProductException> response = handler.handleUnrecognizedField(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(MALFORMED_JSON_OR_FIELD_TYPES, Objects.requireNonNull(response.getBody()).getMessage());

        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
    }
}
