package com.ordenes.ordernservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ordenes.ordernservice.dto.OrdenDto;
import com.ordenes.ordernservice.dto.OrdenKafkaDto;
import com.ordenes.ordernservice.dto.StatusUpdateDto;
import com.ordenes.ordernservice.models.Orden;
import com.ordenes.ordernservice.repository.OrdenRepository;
import com.ordenes.ordernservice.response.GeneralResponse;
import com.ordenes.ordernservice.services.CreateOrdenService;
import com.ordenes.ordernservice.services.ReadOrdenService;
import com.ordenes.ordernservice.services.UpdateOrdenService;
import com.ordenes.ordernservice.kafka.OrderKafkaProducer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@Log4j2
public class OrdenController {
    
    private final CreateOrdenService createService;
    private final ReadOrdenService readService;
    private final UpdateOrdenService updateService;
    private final OrderKafkaProducer orderKafkaProducer;

    // 1. Obtener todas las órdenes
    @GetMapping
    public ResponseEntity<GeneralResponse<List<Orden>>> obtenerTodas() {
        log.info("Petición GET recibida: Solicitando lista completa de órdenes");
        List<Orden> ordenes = readService.obtenerTodas();
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Lista de órdenes obtenida", ordenes));
    }

    // 2. Crear Orden
    @PostMapping
    public ResponseEntity<GeneralResponse<Orden>> crearOrden(@Valid @RequestBody OrdenDto request) {
        log.info("Petición POST recibida: Creando orden para el usuario [{}]", request.getUsuarioCorreo());
        
        Orden ordenCreada = createService.crearOrden(request);
        
        log.info("Petición POST exitosa: Orden creada con ID [{}] y status inicial [{}]", 
                ordenCreada.getId(), ordenCreada.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GeneralResponse<>("SUCCESS", "Orden creada exitosamente", ordenCreada));
    }

    // 2. Obtener Orden por ID
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<Orden>> obtenerPorId(@PathVariable String id) {
        log.info("Petición GET recibida: Buscando orden con ID [{}]", id);
        
        Orden orden = readService.obtenerPorId(id);
        
        log.info("Petición GET exitosa: Orden encontrada con ID [{}]", orden.getId());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Detalles de la orden obtenidos", orden));
    }

    // 3. Obtener Órdenes por Correo (Usuario)
    @GetMapping("/usuario/{id}")
    public ResponseEntity<GeneralResponse<List<Orden>>> obtenerPorUsuarioCorreo(@PathVariable("id") String correo) {
        log.info("Petición GET recibida: Buscando historial de órdenes para el correo [{}]", correo);
        
        List<Orden> ordenes = readService.obtenerPorUsuarioCorreo(correo);
        
        log.info("Petición GET exitosa: Se encontraron {} órdenes para el correo [{}]", ordenes.size(), correo);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Historial de órdenes encontrado", ordenes));
    }

    // 4. Actualizar Status de la Orden
    @PutMapping("/{id}/status")
    public ResponseEntity<GeneralResponse<Orden>> actualizarStatus(
            @PathVariable String id, 
            @Valid @RequestBody StatusUpdateDto request) {
            
        log.info("Petición PUT recibida: Solicitud para cambiar status de la orden [{}] a [{}]", id, request.getStatus());
        
        Orden ordenActualizada = updateService.actualizarStatus(id, request.getStatus());
        
        log.info("Petición PUT exitosa: Status de la orden [{}] actualizado a [{}]", id, ordenActualizada.getStatus());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Status de la orden actualizado", ordenActualizada));
    }

    @PostMapping("/fail")
    public ResponseEntity<GeneralResponse<String>> failOrderProcessing(@Valid @RequestBody OrdenKafkaDto orden) {
        log.error("Petición POST /fail recibida: Simulando fallo para la orden [{}]", orden.getCodigoOrden());
        
        createService.crearOrdenFail(orden); 
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GeneralResponse<>("ERROR", "Error simulado al procesar la orden", null));
    }

    // Método consolidado de OrderController para actualizar status vía Kafka
    @PutMapping("/{id}/update-event")
    public ResponseEntity<GeneralResponse<String>> sendUpdateEvent(@PathVariable String id, @RequestBody Map<String, String> body) {
        log.info("Enviando evento de actualización de status para la orden [{}]", id);
        orderKafkaProducer.sendStatusChanged(new com.ordenes.ordernservice.dto.OrderEvent(id, body.get("email"), body.get("status")));
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Evento de actualización enviado", null));
    }

    @GetMapping("/check-producto/{nombre}")
    public ResponseEntity<Boolean> checkProductoEnOrden(@PathVariable String nombre) {
        log.info("Verificando si el producto [{}] está asociado a alguna orden", nombre);
        boolean existe = readService.existeProductoEnOrdenes(nombre);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/check-producto-id/{id}")
    public ResponseEntity<Boolean> checkProductoIdEnOrden(@PathVariable String id) {
        log.info("Verificando si el producto con ID [{}] está asociado a alguna orden", id);
        boolean existe = readService.existeProductoIdEnOrdenes(id);
        return ResponseEntity.ok(existe);
    }
}
