package com.pagos.pagoservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoKafkaDto {
    
    @NotBlank(message = "El campo ordenid no puede estar vacío")
    @Size(min = 1, max = 255, message = "El campo ordenid no puede tener más de 255 caracteres") 
    private String ordenid;

    @NotNull(message = "El campo fecha no puede estar vacío")
    private Date fecha;

    @NotNull(message = "El campo cantidadPago no puede estar vacío")
    @Positive(message = "El campo cantidadPago debe ser un número positivo") 
    private Double cantidadPago;
}
