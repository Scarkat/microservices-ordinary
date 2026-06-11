package com.ordenes.ordernservice.kafka;

import com.ordenes.ordernservice.dto.InventoryEvent;
import com.ordenes.ordernservice.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderKafkaProducer(@Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStatusChanged(OrderEvent event) {
        kafkaTemplate.send("order_status_changed_events", event.getId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ [KAFKA - MAIN] Evento 'order_status_changed_events' enviado exitosamente para la orden: {}", event.getId());
                } else {
                    log.error("❌ [KAFKA - MAIN] Fallo al enviar evento de estado para la orden {}: {}", event.getId(), ex.getMessage());
                }
            });
    }

    public void sendInventoryUpdate(InventoryEvent event) {
        kafkaTemplate.send("inventory_update_events", event.getProductId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ [KAFKA - MAIN] Evento 'inventory_update_events' enviado para el producto: {}", event.getProductId());
                } else {
                    log.error("❌ [KAFKA - MAIN] Fallo al enviar actualización de inventario para {}: {}", event.getProductId(), ex.getMessage());
                }
            });
    }
}
