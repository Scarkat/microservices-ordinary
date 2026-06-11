package com.ordenes.ordernservice.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.ordenes.ordernservice.dto.OrdenKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenProducer {
    
    @Value("${kafka.topic.ordenes}")
    private String topic;

    private final KafkaTemplate<String, OrdenKafkaDto> kafkaTemplate;
    
    public void enviarOrden(OrdenKafkaDto orden) {
        try {
            // El .get() es la clave: bloquea el hilo hasta que Kafka responda
            var result = kafkaTemplate.send(topic, orden.getCodigoOrden(), orden).get(); 
            
            log.info("Orden enviada [{}] -> partition: {}, offset: {}",
                    orden.getCodigoOrden(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            
        } catch (Exception ex) {
            log.error("Error crítico al enviar orden a Kafka [{}]: {}", orden.getCodigoOrden(), ex.getMessage());
        }
    }
}