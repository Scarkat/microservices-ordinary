package com.productos.productoservice.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.productos.productoservice.dto.ProductKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProducer {
    
    @Value("${kafka.topic.productos}")
    private String topic;

    private final KafkaTemplate<String, ProductKafkaDto> kafkaTemplate;
    
    public void enviarProducto(ProductKafkaDto product) {
        try {
            // El .get() es la clave: bloquea el hilo hasta que Kafka responda
            var result = kafkaTemplate.send(topic, product.getId(), product).get(); 
            
            log.info("Producto enviado [{}] -> partition: {}, offset: {}",
                    product.getId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            
        } catch (Exception ex) {
            log.error("Error crítico al enviar producto a Kafka [{}]: {}", product.getId(), ex.getMessage());
        }
    }
}
