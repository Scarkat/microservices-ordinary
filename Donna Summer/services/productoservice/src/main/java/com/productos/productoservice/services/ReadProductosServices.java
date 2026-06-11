package com.productos.productoservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.productos.productoservice.models.Products;
import com.productos.productoservice.repository.ProductosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadProductosServices {
    
    private final ProductosRepository repository;

    public List<Products> obtenerTodos() {
        return repository.findAll();
    }

    public Products obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + id));
    }

    public Products obtenerPorNombre(String nombre) {
        return repository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el nombre: " + nombre));
    }
}
