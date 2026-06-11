package com.ordenes.ordernservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ordenes.ordernservice.models.Orden;
import com.ordenes.ordernservice.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadOrdenService {
    
    private final OrdenRepository repository;

    public Orden obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID: " + id));
    }

    public List<Orden> obtenerPorUsuarioCorreo(String correo) {
        List<Orden> ordenes = repository.findByUsuarioCorreo(correo);
        if (ordenes.isEmpty()) {
            log.info("No se encontraron órdenes para el correo: {}", correo);
        }
        return ordenes;
    }

    public List<Orden> obtenerTodas() {
        return repository.findAll();
    }

    public boolean existeProductoEnOrdenes(String nombreProducto) {
        return repository.existsByProductosNombreProducto(nombreProducto);
    }

    public boolean existeProductoIdEnOrdenes(String idProducto) {
        return repository.existsByProductosCodigoProducto(idProducto);
    }
}
