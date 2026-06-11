package com.ordenes.ordernservice.services;

import org.springframework.stereotype.Service;

import com.ordenes.ordernservice.dto.OrderEvent;
import com.ordenes.ordernservice.kafka.OrderKafkaProducer;
import com.ordenes.ordernservice.models.Orden;
import com.ordenes.ordernservice.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateOrdenService {
    
    private final OrdenRepository repository;
    private final OrderKafkaProducer orderKafkaProducer;

    public Orden actualizarStatus(String id, String nuevoStatus) {
        Orden ordenExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar. Orden no encontrada con ID: " + id));

        // Aquí podríamos agregar lógica extra en el futuro (ej. no permitir pasar de "CANCELADA" a "ENVIADA")
        ordenExistente.setStatus(nuevoStatus.toUpperCase()); 

        Orden saved = repository.save(ordenExistente);

        try {
            orderKafkaProducer.sendStatusChanged(new OrderEvent(
                    saved.getId(),
                    saved.getUsuarioCorreo(),
                    saved.getStatus()
            ));
            log.info("Evento Kafka de cambio de estado enviado para la orden: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error enviando evento Kafka de cambio de estado: {}", e.getMessage());
        }

        return saved;
    }
}
