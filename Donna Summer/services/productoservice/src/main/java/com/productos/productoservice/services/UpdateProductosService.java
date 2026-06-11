package com.productos.productoservice.services;

import org.springframework.stereotype.Service;

import com.productos.productoservice.dto.InventoryEvent;
import com.productos.productoservice.dto.ProductoDto;
import com.productos.productoservice.models.Products;
import com.productos.productoservice.repository.ProductosRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProductosService {
    
    private final ProductosRepository repository;
    @Qualifier("genericKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Products actualizarProducto(String id, ProductoDto dto) {
        Products productoExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar. Producto no encontrado con ID: " + id));

        productoExistente.setNombre(dto.getNombre());
        productoExistente.setDescripcion(dto.getDescripcion());
        productoExistente.setPrecio(dto.getPrecio());
        productoExistente.setCantidad(dto.getCantidad());
        productoExistente.setImagen(dto.getImagen());
        productoExistente.setMarca(dto.getMarca());
        productoExistente.setProveedor(dto.getProveedor());
        productoExistente.setCategoria(dto.getCategoria());

        return repository.save(productoExistente);
    }

    public Products reducirInventario(String nombre, int cantidadARestar) {
        Products producto = repository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + nombre));

        return reducirStock(producto, cantidadARestar);
    }

    public Products reducirInventarioPorId(String id, int cantidadARestar) {
        Products producto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        return reducirStock(producto, cantidadARestar);
    }

    private Products reducirStock(Products producto, int cantidadARestar) {
        int nuevoStock = producto.getCantidad() - cantidadARestar;
        if (nuevoStock < 0) {
            log.warn("Stock insuficiente para {}, dejando en 0", producto.getNombre());
            nuevoStock = 0;
        }

        producto.setCantidad(nuevoStock);
        Products saved = repository.save(producto);
        
        log.info("Inventario reducido para {} (ID: {}): {} -> {}", 
                producto.getNombre(), producto.getId(), producto.getCantidad() + cantidadARestar, nuevoStock);
        return saved;
    }
}
