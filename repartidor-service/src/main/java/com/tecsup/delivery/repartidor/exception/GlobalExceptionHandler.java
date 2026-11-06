package com.tecsup.delivery.repartidor.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorDetalle> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::convertir)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "JSON malformado o valor inválido", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> manejarTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName(), request, null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> manejarConflicto(ConflictoException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error interno no controlado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, null);
    }

    private ErrorDetalle convertir(FieldError error) {
        return new ErrorDetalle(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiError> construir(HttpStatus estado, String mensaje, HttpServletRequest request, List<ErrorDetalle> errores) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, request.getRequestURI(), errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
