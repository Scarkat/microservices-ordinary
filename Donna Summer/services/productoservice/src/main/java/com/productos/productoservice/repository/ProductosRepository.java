package com.productos.productoservice.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.productos.productoservice.models.Products;

public interface ProductosRepository extends MongoRepository<Products, String>  {
    
    Optional<Products> findByNombre(String nombre);
}
