package com.pagos.pagoservice.models;

import java.util.Date;

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

@Document(collection = "pagos")
public class Pagos {
    @Id
    private String id;
    private String ordenid;
    private Date fecha;
    private Double cantidadPago;
    private String status;
}
