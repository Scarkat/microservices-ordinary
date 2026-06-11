package com.productos.productoservice.services;

import com.productos.productoservice.kafka.ProductProducer;

import org.springframework.stereotype.Service;

import com.productos.productoservice.dto.ProductKafkaDto;
import com.productos.productoservice.dto.ProductoDto;
import com.productos.productoservice.models.Products;
import com.productos.productoservice.repository.ProductosRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductosService {
    
    private final ProductProducer productProducer;
    private final ProductosRepository repository;

    public Products crearProducto(ProductoDto dto) {
        Products nuevoProducto = new Products();
        
        nuevoProducto.setNombre(dto.getNombre());
        nuevoProducto.setDescripcion(dto.getDescripcion());
        nuevoProducto.setPrecio(dto.getPrecio());
        nuevoProducto.setCantidad(dto.getCantidad());
        nuevoProducto.setImagen(dto.getImagen());
        nuevoProducto.setMarca(dto.getMarca());
        nuevoProducto.setProveedor(dto.getProveedor());
        nuevoProducto.setCategoria(dto.getCategoria());
        
        try {
            return repository.save(nuevoProducto);
        } catch (Exception e) {
            log.error("Error crítico al guardar el producto en la base de datos: {}", e.getMessage());
            
            // Enviamos mensaje de error a Kafka para el flujo de reintento
            try {
                // Como no tenemos un ID de base de datos todavía, usamos un marcador o el nombre como clave temporal si fuera necesario
                // Pero ProductKafkaDto requiere un ID. Usaremos un ID generado o el nombre para la prueba de reintento.
                ProductKafkaDto failDto = new ProductKafkaDto(
                    "TEMP-" + System.currentTimeMillis(),
                    dto.getNombre(),
                    dto.getDescripcion(),
                    dto.getPrecio(),
                    dto.getCantidad(),
                    dto.getImagen(),
                    dto.getMarca(),
                    dto.getProveedor(),
                    dto.getCategoria()
                );
                productProducer.enviarProducto(failDto);
                log.info("Mensaje de fallo enviado a Kafka para el producto: {}", dto.getNombre());
            } catch (Exception kafkaEx) {
                log.error("No se pudo enviar el mensaje de fallo a Kafka: {}", kafkaEx.getMessage());
            }
            
            throw e;
        }
    }

    public Products crearProductoFail(ProductKafkaDto productoFailed) {
        
        // Simulamos un error lanzando una excepción personalizada
        log.error("Error al crear producto [{}]: {}", productoFailed.getId(), "Simulación de error en la creación del producto");
        productProducer.enviarProducto(productoFailed);
        throw new RuntimeException("Error simulado al crear el producto");
    }
}
