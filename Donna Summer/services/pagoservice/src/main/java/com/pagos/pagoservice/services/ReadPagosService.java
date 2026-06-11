package com.pagos.pagoservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pagos.pagoservice.models.Pagos;
import com.pagos.pagoservice.repository.PagosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadPagosService {
    private final PagosRepository repository;

    // 1. Obtener todos los pagos
    public List<Pagos> obtenerTodos() {
        return repository.findAll();
    }

    // 2. Obtener por ID del pago
    public Pagos obtenerPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con el ID: " + id));
    }

    // 3. Obtener por ID de la orden
    public List<Pagos> obtenerPorOrdenId(String ordenid) {
        List<Pagos> pagos = repository.findByOrdenid(ordenid);
        if (pagos.isEmpty()) {
            throw new RuntimeException("No se encontraron pagos para la orden con ID: " + ordenid);
        }
        return pagos;
    }
}
