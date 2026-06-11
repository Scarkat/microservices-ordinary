package com.productos.productoservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    @NotBlank(message = "El nombre del producto no puede estar vacío ni contener solo espacios")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double precio;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad en inventario no puede ser negativa")
    private Integer cantidad;

    @NotBlank(message = "La imagen es obligatoria")
    private String imagen; 

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    private String marca;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;
}
