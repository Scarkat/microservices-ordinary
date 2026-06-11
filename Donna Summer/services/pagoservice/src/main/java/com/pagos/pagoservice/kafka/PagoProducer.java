package com.pagos.pagoservice.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.pagos.pagoservice.dto.PagoKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoProducer {
    
    @Value("${kafka.topic.pagos}")
    private String topic;

    private final KafkaTemplate<String, PagoKafkaDto> kafkaTemplate;
    
    public void enviarPago(PagoKafkaDto pago) {
        try {
            // El .get() es la clave: bloquea el hilo hasta que Kafka responda
            var result = kafkaTemplate.send(topic, pago.getOrdenid(), pago).get(); 
            
            log.info("Pago enviado [{}] -> partition: {}, offset: {}",
                    pago.getOrdenid(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            
        } catch (Exception ex) {
            log.error("Error crítico al enviar pago a Kafka [{}]: {}", pago.getOrdenid(), ex.getMessage());
        }
    }
}
