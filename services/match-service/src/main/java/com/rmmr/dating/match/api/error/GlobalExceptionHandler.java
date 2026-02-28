package com.rmmr.dating.match.api.error;

import com.rmmr.dating.match.domain.ex.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MatchNotFoundException.class)
    public ResponseEntity<ApiError> notFound(MatchNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "Match no encontrado.", req);
    }

    @ExceptionHandler(NotParticipantException.class)
    public ResponseEntity<ApiError> notParticipant(NotParticipantException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "NOT_A_PARTICIPANT", "No tienes acceso a este match.", req);
    }

    @ExceptionHandler(MatchExpiredException.class)
    public ResponseEntity<ApiError> expired(MatchExpiredException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "MATCH_EXPIRED", "Este match ya expiró.", req);
    }

    @ExceptionHandler(FirstMoverOnlyException.class)
    public ResponseEntity<ApiError> firstMover(FirstMoverOnlyException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "FIRST_MOVER_ONLY", "Solo la persona que inicia puede mandar el primer mensaje.", req);
    }

    @ExceptionHandler(InvalidMatchStateException.class)
    public ResponseEntity<ApiError> invalidState(InvalidMatchStateException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "INVALID_MATCH_STATE", "El match no está en un estado válido para esta operación.", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Datos inválidos en la petición.", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONFLICT", "Conflicto al procesar la operación.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error inesperado.", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        var body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}