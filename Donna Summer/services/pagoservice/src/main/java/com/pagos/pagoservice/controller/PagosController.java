package com.pagos.pagoservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pagos.pagoservice.dto.PagoKafkaDto;
import com.pagos.pagoservice.dto.PagosDto;
import com.pagos.pagoservice.dto.PaymentEvent;
import com.pagos.pagoservice.dto.PaymentRequest;
import com.pagos.pagoservice.kafka.PaymentKafkaProducer;
import com.pagos.pagoservice.models.Pagos;
import com.pagos.pagoservice.response.GeneralResponse;
import com.pagos.pagoservice.services.CreatePagosService;
import com.pagos.pagoservice.services.ReadPagosService;
import com.pagos.pagoservice.services.UpdatePagosService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/pagos")
@Log4j2
@RequiredArgsConstructor
public class PagosController {
    
    private final CreatePagosService createService;
    private final ReadPagosService readService;
    private final UpdatePagosService updateService;
    private final PaymentKafkaProducer paymentKafkaProducer;

    // 1. Crear un pago (Por defecto nace como PROCESADO)
    @PostMapping("/procesar")
    public ResponseEntity<GeneralResponse<Pagos>> procesarPago(@Valid @RequestBody PagosDto request) {
        log.info("Petición POST recibida: Intentando procesar pago para la orden [{}] por la cantidad de [{}]", 
                request.getOrdenid(), request.getCantidadPago());
        
        Pagos pagoCreado = createService.crearPago(request);
        
        log.info("Petición POST exitosa: Pago procesado y guardado con ID [{}]", pagoCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GeneralResponse<>("SUCCESS", "Pago procesado exitosamente", pagoCreado));
    }

    // 2. Obtener todos los pagos (Extra, para panel de administración)
    @GetMapping
    public ResponseEntity<GeneralResponse<List<Pagos>>> obtenerTodos() {
        log.info("Petición GET recibida: Solicitando el historial completo de pagos");
        
        List<Pagos> listaPagos = readService.obtenerTodos();
        
        log.info("Petición GET exitosa: Se devolvieron {} pagos", listaPagos.size());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Historial de pagos obtenido", listaPagos));
    }

    // 3. Obtener pago por ID de pago
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<Pagos>> obtenerPorId(@PathVariable String id) {
        log.info("Petición GET recibida: Buscando detalles del pago con ID [{}]", id);
        
        Pagos pago = readService.obtenerPorId(id);
        
        log.info("Petición GET exitosa: Pago encontrado con ID [{}]", pago.getId());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Detalles del pago obtenidos", pago));
    }

    // 4. Obtener pagos vinculados a un ID de orden específica
    @GetMapping("/orden/{id}")
    public ResponseEntity<GeneralResponse<List<Pagos>>> obtenerPorOrdenId(@PathVariable("id") String ordenid) {
        log.info("Petición GET recibida: Buscando pagos asociados a la orden [{}]", ordenid);
        
        List<Pagos> pagos = readService.obtenerPorOrdenId(ordenid);
        
        log.info("Petición GET exitosa: Se encontraron {} pagos para la orden [{}]", pagos.size(), ordenid);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Pagos de la orden encontrados", pagos));
    }

    // 5. Reembolsar un pago
    @PutMapping("/{id}/reembolso")
    public ResponseEntity<GeneralResponse<Pagos>> reembolsarPago(@PathVariable String id) {
        // Usamos log.warn porque es una operación financiera sensible/destructiva
        log.warn("Petición PUT recibida: Solicitud de REEMBOLSO iniciada para el pago con ID [{}]", id);
        
        Pagos pagoReembolsado = updateService.reembolsarPago(id);
        
        log.info("Petición PUT exitosa: El pago con ID [{}] ha cambiado su estado a REEMBOLSADO", id);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Pago reembolsado exitosamente", pagoReembolsado));
    }

    // Error forzado en Kafka (Adaptado para Pagos)
    @PostMapping("/fail")
    public ResponseEntity<GeneralResponse<String>> failPaymentProcessing(@Valid @RequestBody PagoKafkaDto pago) {
        log.error("Petición POST /fail recibida: Simulando fallo de pago para la orden [{}]", pago.getOrdenid());
        
        createService.crearPagoFail(pago); 
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GeneralResponse<>("ERROR", "Error simulado al procesar the pago", null));
    }

    // Método consolidado de PaymentController para enviar evento de pago recibido
    @PostMapping("/payment-event")
    public ResponseEntity<GeneralResponse<String>> sendPaymentEvent(@RequestBody PaymentRequest request) {
        log.info("Enviando evento de pago recibido para la orden [{}]", request.getOrderId());
        paymentKafkaProducer.sendPaymentReceived(new PaymentEvent(
                request.getOrderId(), 
                request.getEmail(), 
                String.valueOf(request.getAmount()), 
                true));
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Evento de pago enviado", null));
    }
}
