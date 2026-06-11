package com.productos.productoservice.services;

import org.springframework.stereotype.Service;

import com.productos.productoservice.models.Products;
import com.productos.productoservice.repository.ProductosRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteProdcutosService {
    
    private final ProductosRepository repository;
    private final RestTemplate restTemplate;

    public void eliminarProducto(String id) {
        Products producto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PRODUCTO_NO_ENCONTRADO: No se puede eliminar. Producto no encontrado con ID: " + id));
        
        log.info("Iniciando validación de eliminación para producto: {} (ID: {})", producto.getNombre(), producto.getId());

        // Regla: No se puede eliminar si el producto está asociado a una orden
        try {
            String url = "http://ordernservice:8080/ordenes/check-producto-id/" + producto.getId();
            log.info("Consultando al servicio de órdenes: {}", url);
            Boolean existeEnOrden = restTemplate.getForObject(url, Boolean.class);
            
            log.info("Respuesta del servicio de órdenes: {}", existeEnOrden);

            if (Boolean.TRUE.equals(existeEnOrden)) {
                log.warn("RECHAZADO: Intento de eliminar producto [{}] (ID: {}) rechazado: está asociado a una orden", producto.getNombre(), producto.getId());
                throw new RuntimeException("PRODUCTO_ASOCIADO: No se puede eliminar el producto porque ya está asociado a una o más órdenes.");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("PRODUCTO_ASOCIADO")) {
                throw e;
            }
            log.error("Fallo de comunicación con ordernservice: {}", e.getMessage());
            throw new RuntimeException("ERROR_VALIDACION: No se pudo verificar la asociación del producto con el sistema de órdenes.");
        }

        repository.delete(producto);
        log.info("Producto {} eliminado correctamente", id);
    }
}
