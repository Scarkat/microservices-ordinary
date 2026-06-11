package com.productos.productoservice.kafka;

import com.productos.productoservice.dto.InventoryEvent;
import com.productos.productoservice.services.UpdateProductosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final UpdateProductosService updateService;

    @KafkaListener(topics = "inventory_update_events", groupId = "productos-group")
    public void consumeInventoryUpdate(InventoryEvent event) {
        log.info("Evento de inventario recibido para ID: {} (Cantidad a restar: {})", 
                event.getProductId(), event.getQuantity());
        
        try {
            updateService.reducirInventarioPorId(event.getProductId(), event.getQuantity());
        } catch (Exception e) {
            log.error("Error al procesar reducción de inventario: {}", e.getMessage());
        }
    }
}
