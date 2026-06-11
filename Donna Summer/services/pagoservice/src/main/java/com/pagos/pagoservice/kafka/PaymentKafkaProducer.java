package com.pagos.pagoservice.kafka;

import com.pagos.pagoservice.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentKafkaProducer(@Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentReceived(PaymentEvent event) {
        kafkaTemplate.send("payment_received_events", event.getOrderId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ [KAFKA - MAIN] Evento 'payment_received_events' enviado exitosamente para la orden: {}", event.getOrderId());
                } else {
                    log.error("❌ [KAFKA - MAIN] Fallo al enviar evento de pago para la orden {}: {}", event.getOrderId(), ex.getMessage());
                }
            });
    }
}
