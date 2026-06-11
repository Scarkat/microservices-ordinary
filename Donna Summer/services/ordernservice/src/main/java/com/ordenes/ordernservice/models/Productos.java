package com.ordenes.ordernservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Productos {

    private String codigoProducto;
    private String nombreProducto;
    private Double precio;
    private Integer cantidad;
    
}
