package com.pagos.pagoservice.services;

import org.springframework.stereotype.Service;

import com.pagos.pagoservice.dto.PaymentEvent;
import com.pagos.pagoservice.dto.PagoKafkaDto;
import com.pagos.pagoservice.dto.PagosDto;
import com.pagos.pagoservice.kafka.PagoProducer;
import com.pagos.pagoservice.kafka.PaymentKafkaProducer;
import com.pagos.pagoservice.models.Pagos;
import com.pagos.pagoservice.repository.PagosRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CreatePagosService {
    private final PagosRepository repository;
    private final PagoProducer pagoProducer;
    private final PaymentKafkaProducer paymentKafkaProducer;

    public Pagos crearPago(PagosDto dto) {
        Pagos nuevoPago = new Pagos();
        nuevoPago.setOrdenid(dto.getOrdenid());
        nuevoPago.setFecha(dto.getFecha());
        nuevoPago.setCantidadPago(dto.getCantidadPago());
        
        // Regla de negocio inquebrantable: Todo pago nuevo nace como PROCESADO
        nuevoPago.setStatus("PROCESADO"); 
        
        try {
            Pagos savedPago = repository.save(nuevoPago);

            // --- NOTIFICACIÓN AL FLUJO PRINCIPAL (KAFKA) ---
            try {
                paymentKafkaProducer.sendPaymentReceived(new PaymentEvent(
                        savedPago.getOrdenid(),
                        "usuario@ejemplo.com", // Puedes extraer esto si lo añades al DTO o Pago
                        String.valueOf(savedPago.getCantidadPago()),
                        true
                ));
                log.info("Evento de Kafka 'payment_received_events' enviado para la orden: {}", savedPago.getOrdenid());
            } catch (Exception e) {
                log.error("Error enviando evento de pago a Kafka: {}", e.getMessage());
            }

            return savedPago;
        } catch (Exception e) {
            log.error("Error crítico al guardar el pago en la base de datos: {}", e.getMessage());
            
            // Enviamos mensaje de error a Kafka para el flujo de reintento
            try {
                PagoKafkaDto failDto = new PagoKafkaDto(
                    dto.getOrdenid(),
                    dto.getFecha(),
                    dto.getCantidadPago()
                );
                pagoProducer.enviarPago(failDto);
                log.info("Mensaje de fallo enviado a Kafka para el pago de la orden: {}", dto.getOrdenid());
            } catch (Exception kafkaEx) {
                log.error("No se pudo enviar el mensaje de fallo a Kafka: {}", kafkaEx.getMessage());
            }
            
            throw e;
        }
    }

    public void crearPagoFail(PagoKafkaDto pagoFailed) {
        // Simulamos un error lanzando una excepción personalizada
        log.error("Error al procesar pago para la orden [{}]: {}", pagoFailed.getOrdenid(), "Simulación de error en la creación del pago");
        
        // Enviamos el mensaje a Kafka para que otros servicios se enteren del fallo
        pagoProducer.enviarPago(pagoFailed);
        
        throw new RuntimeException("Error simulado al procesar el pago");
    }
}
