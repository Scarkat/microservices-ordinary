package com.queries.querie.kafka;

import com.queries.querie.entities.postgres.Envio;
import com.queries.querie.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingConsumer {

    private final EnvioRepository envioRepository;

    @KafkaListener(topics = "payment_received_events", groupId = "querie-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Evento de pago recibido para la orden: {}. FullyPaid: {}", 
                event.getOrderId(), event.isFullyPaid());
        
        if (event.isFullyPaid()) {
            log.info("Orden {} pagada totalmente. Registrando envío...", event.getOrderId());
            try {
                Envio envio = Envio.builder()
                        .orderId(event.getOrderId())
                        .email(event.getEmailCliente())
                        .totalPaid(Double.parseDouble(event.getAmount()))
                        .fechaEnvio(new Date())
                        .build();
                
                envioRepository.save(envio);
                log.info("Envío registrado exitosamente para la orden: {}", event.getOrderId());
            } catch (Exception e) {
                log.error("Error al registrar el envío: {}", e.getMessage());
            }
        }
    }
}
