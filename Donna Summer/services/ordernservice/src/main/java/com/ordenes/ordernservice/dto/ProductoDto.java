package com.ordenes.ordernservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
   
    @NotBlank(message = "El campo 'codigoProducto' no puede estar vacío")
    @Size(min = 1, max = 50, message = "El campo 'codigoProducto' no puede tener más de 50 caracteres")
    private String codigoProducto;

    @NotBlank(message = "El campo 'nombreProducto' no puede estar vacío")
    @Size(min = 1, max = 150, message = "El campo 'nombreProducto' no puede tener más de 150 caracteres")
    private String nombreProducto;

    @NotNull(message = "El campo 'precio' es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo mayor a cero")
    private Double precio;

    @NotNull(message = "El campo 'cantidad' es obligatorio")
    @PositiveOrZero(message = "La cantidad no puede ser un número negativo")
    private Integer cantidad;
}
