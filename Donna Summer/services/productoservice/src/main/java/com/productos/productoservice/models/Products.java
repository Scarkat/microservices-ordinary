package com.productos.productoservice.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "productos")
public class Products {
    
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer cantidad;
    private String imagen;
    private String marca;
    private String proveedor;
    private String categoria;
}
