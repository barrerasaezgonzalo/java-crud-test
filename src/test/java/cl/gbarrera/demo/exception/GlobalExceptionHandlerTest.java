package cl.gbarrera.demo.exception;

import cl.gbarrera.demo.infrastructure.web.dto.ErrorResponse;
import cl.gbarrera.demo.infrastructure.web.exception.GlobalExceptionHandler;
import cl.gbarrera.demo.product.application.exception.InvalidProductException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.persistence.OptimisticLockException;
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
        when(ex.getPropertyName()).thenReturn("extraField");

        ResponseEntity<ErrorResponse> response = handler.handleUnrecognizedField(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorBody = Objects.requireNonNull(response.getBody());

        assertEquals(MALFORMED_JSON_OR_FIELD_TYPES, errorBody.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorBody.getStatus());
        assertEquals("/api/test", errorBody.getPath());
        assertNotNull(errorBody.getTimestamp());
        assertNotNull(errorBody.getRequestId());
    }

    @Test
    void handleOptimisticLockException_shouldReturnConflict() {
        when(request.getRequestURI()).thenReturn("/api/v1/products/1");

        OptimisticLockException ex = mock(OptimisticLockException.class);
        when(ex.getMessage()).thenReturn("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)");

        final String EXPECTED_ERROR_MESSAGE = "The resource you are trying to update has been modified by another user. Please re-read and try again.";

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorBody = Objects.requireNonNull(response.getBody());

        assertEquals(HttpStatus.CONFLICT.value(), errorBody.getStatus());
        assertEquals(EXPECTED_ERROR_MESSAGE, errorBody.getError());
        assertEquals("/api/v1/products/1", errorBody.getPath());
        assertNotNull(errorBody.getTimestamp());
        assertNotNull(errorBody.getRequestId());
    }
}
