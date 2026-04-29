package br.com.fiap.upload.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex,
                                                               HttpServletRequest req) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex,
                                                                 HttpServletRequest req) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileTooLarge(MaxUploadSizeExceededException ex,
                                                                   HttpServletRequest req) {
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE,
                "Arquivo excede o tamanho máximo permitido de 10MB.", req.getRequestURI());
    }

    @ExceptionHandler({
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class,
        HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<Map<String, Object>> handleMissingPart(Exception ex, HttpServletRequest req) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Requisição inválida: envie o arquivo no campo 'file' com Content-Type multipart/form-data.",
                req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Requisição inválida");
        return buildError(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erro interno não tratado. path={}", req.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor. Tente novamente mais tarde.", req.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path
        ));
    }
}
