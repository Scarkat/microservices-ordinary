package com.ordenes.ordernservice.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.ordenes.ordernservice.dto.InventoryEvent;
import com.ordenes.ordernservice.dto.OrderEvent;
import com.ordenes.ordernservice.dto.OrdenDto;
import com.ordenes.ordernservice.dto.OrdenKafkaDto;
import com.ordenes.ordernservice.kafka.OrdenProducer;
import com.ordenes.ordernservice.kafka.OrderKafkaProducer;
import com.ordenes.ordernservice.models.Orden;
import com.ordenes.ordernservice.models.Productos;
import com.ordenes.ordernservice.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrdenService {
    
    private final OrdenRepository repository;
    private final OrdenProducer ordenProducer;
    private final OrderKafkaProducer orderKafkaProducer;
    private final RestTemplate restTemplate;

    public Orden crearOrden(OrdenDto dto) {
        
        // --- VALIDACIÓN DE STOCK SÍNCRONA ---
        if (dto.getProductos() != null) {
            for (Productos p : dto.getProductos()) {
                try {
                    log.info("Validando stock para producto [{}] ID [{}] Cantidad [{}]", p.getNombreProducto(), p.getCodigoProducto(), p.getCantidad());
                    String url = "http://productoservice:8080/productos/validar-stock-id/" + p.getCodigoProducto() + "/" + p.getCantidad();
                    Boolean hayStock = restTemplate.getForObject(url, Boolean.class);
                    
                    if (hayStock == null || Boolean.FALSE.equals(hayStock)) {
                        log.warn("RESULTADO: Stock insuficiente o producto no encontrado para ID [{}]", p.getCodigoProducto());
                        throw new RuntimeException("STOCK_INSUFICIENTE: No hay suficiente stock para " + p.getNombreProducto());
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("STOCK_INSUFICIENTE")) {
                        throw e;
                    }
                    log.error("Fallo de comunicación con productoservice para ID [{}]: {}", p.getCodigoProducto(), e.getMessage());
                    throw new RuntimeException("ERROR_COMUNICACION: No se pudo verificar el stock del producto " + p.getNombreProducto());
                }
            }
        }

        Orden nuevaOrden = new Orden();
        
        // 1. Mapeamos los datos confiables que vienen del usuario
        nuevaOrden.setCodigoOrden(dto.getCodigoOrden());
        nuevaOrden.setUsuarioCorreo(dto.getUsuarioCorreo());
        
        // Guardamos la fotografía histórica de los productos
        nuevaOrden.setProductos(dto.getProductos());

        // Regla 1: La fecha es la del servidor en el momento exacto
        nuevaOrden.setFecha(new Date()); 
        
        // Regla 2: Status inicial inquebrantable
        nuevaOrden.setStatus("CREADA");
        
        // Regla 3: Cálculo del total en el backend (Prevención de fraude)
        double totalCalculado = 0.0;
        if (dto.getProductos() != null && !dto.getProductos().isEmpty()) {
            for (Productos producto : dto.getProductos()) {
                // Multiplicamos el precio histórico por la cantidad solicitada
                totalCalculado += (producto.getPrecio() * producto.getCantidad());
            }
        }
        nuevaOrden.setTotal(totalCalculado);

        // Finalmente, guardamos la orden blindada
        try {
            Orden savedOrden = repository.save(nuevaOrden);

            // --- NOTIFICACIÓN AL FLUJO PRINCIPAL (KAFKA) ---
            try {
                // 1. Evento de cambio de estado
                orderKafkaProducer.sendStatusChanged(new OrderEvent(
                        savedOrden.getId(), 
                        savedOrden.getUsuarioCorreo(), 
                        savedOrden.getStatus()
                ));

                // 2. Evento de actualización de inventario (por cada producto)
                if (savedOrden.getProductos() != null) {
                    for (Productos p : savedOrden.getProductos()) {
                        orderKafkaProducer.sendInventoryUpdate(new InventoryEvent(
                                p.getCodigoProducto(), // Usando el ID único para mayor precisión
                                p.getCantidad()
                        ));
                    }
                }
                log.info("Eventos de Kafka enviados para la orden: {}", savedOrden.getId());
            } catch (Exception e) {
                log.error("Error enviando eventos de Kafka: {}", e.getMessage());
            }

            return savedOrden;
        } catch (Exception e) {
            log.error("Error crítico al guardar la orden en la base de datos: {}", e.getMessage());
            
            // Enviamos mensaje de error a Kafka para el flujo de reintento
            try {
                OrdenKafkaDto failDto = new OrdenKafkaDto(
                    dto.getCodigoOrden(),
                    dto.getUsuarioCorreo(),
                    dto.getProductos()
                );
                ordenProducer.enviarOrden(failDto);
                log.info("Mensaje de fallo enviado a Kafka para la orden: {}", dto.getCodigoOrden());
            } catch (Exception kafkaEx) {
                log.error("No se pudo enviar el mensaje de fallo a Kafka: {}", kafkaEx.getMessage());
            }
            
            throw e; // Relanzamos para que el GlobalExceptionHandler lo capture
        }
    }

    public void crearOrdenFail(OrdenKafkaDto ordenFailed) {
        // Simulamos un error lanzando una excepción personalizada
        log.error("Error al procesar la orden [{}]: {}", ordenFailed.getCodigoOrden(), "Simulación de error en la creación de la orden");
        
        // Enviamos el mensaje a Kafka para que otros servicios se enteren del fallo
        ordenProducer.enviarOrden(ordenFailed);
        
        throw new RuntimeException("Error simulado al procesar la orden");
    }
}
