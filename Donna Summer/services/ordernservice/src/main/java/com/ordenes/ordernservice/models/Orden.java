package com.ordenes.ordernservice.models;

import java.util.Date;
import java.util.List;

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

@Document(collection = "ordenes")
public class Orden {
    
    @Id
    private String id;
    private String codigoOrden;
    private Date fecha;
    private Double total;
    private String status;
    private String usuarioCorreo;
    private List<Productos> productos;
}
