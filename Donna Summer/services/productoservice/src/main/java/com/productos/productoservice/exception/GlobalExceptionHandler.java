package com.productos.productoservice.exception;

import java.util.HashMap;
import java.util.Map;


import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.productos.productoservice.response.GeneralResponse;


@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        Map<String, String> errores = new HashMap<>();
        
        // Extraemos cada campo que falló y su mensaje de error personalizado
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensajeError = error.getDefaultMessage();
            errores.put(nombreCampo, mensajeError);
        });

        log.warn("Petición bloqueada por validación de datos. Errores: {}", errores);

        // Empaquetamos los errores en tu estructura estándar
        GeneralResponse<Map<String, String>> response = new GeneralResponse<>(
                "BAD_REQUEST",
                "Los datos enviados no cumplen con las reglas de validación",
                errores 
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Opcional: Atrapa cualquier otra excepción no controlada para que nunca devuelvas un 500 feo
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralResponse<String>> handleAllUncaughtException(Exception ex) {
        log.error("Error interno no controlado: ", ex);
        
        String customMessage = "Ocurrió un error inesperado en el servidor";
        if (ex.getMessage() != null && (ex.getMessage().contains("PRODUCTO_ASOCIADO") || ex.getMessage().contains("ERROR_VALIDACION") || ex.getMessage().contains("PRODUCTO_NO_ENCONTRADO"))) {
            customMessage = ex.getMessage();
        }

        GeneralResponse<String> response = new GeneralResponse<>(
                "INTERNAL_ERROR",
                customMessage,
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
