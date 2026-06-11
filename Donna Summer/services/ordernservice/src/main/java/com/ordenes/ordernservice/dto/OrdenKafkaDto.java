package com.ordenes.ordernservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import com.ordenes.ordernservice.models.Productos;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenKafkaDto { 
    
    @NotBlank(message = "El campo 'codigoOrden' no puede estar vacío")
    @Size(min=1, max = 50, message = "El campo 'codigoOrden' no puede tener más de 50 caracteres")
    private String codigoOrden;
    
    @NotBlank(message = "El campo 'usuarioCorreo' no puede estar vacío")
    @Email(message = "El formato del correo electrónico no es válido") // ¡Mejora de calidad!
    @Size(min=1, max = 100, message = "El campo 'usuarioCorreo' no puede tener más de 100 caracteres")
    private String usuarioCorreo;
    
    @NotEmpty(message = "La lista de 'productos' no puede estar vacía")
    @Valid 
    private List<Productos> productos;
}
