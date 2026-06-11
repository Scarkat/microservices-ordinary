package com.ordenes.ordernservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ordenes.ordernservice.models.Orden;

public interface OrdenRepository extends MongoRepository<Orden, String> {

    Optional<Orden> findByCodigoOrden(String codigoOrden);

    List<Orden> findByUsuarioCorreo(String usuarioCorreo);
    
    Optional<List<Orden>> findByStatus(String status);
    
    @org.springframework.data.mongodb.repository.Query(value = "{ 'productos.nombreProducto': ?0 }", exists = true)
    boolean existsByProductosNombreProducto(String nombreProducto);
    
    @org.springframework.data.mongodb.repository.Query(value = "{ 'productos.codigoProducto': ?0 }", exists = true)
    boolean existsByProductosCodigoProducto(String codigoProducto);
    
}