package com.pagos.pagoservice.services;

import org.springframework.stereotype.Service;

import com.pagos.pagoservice.models.Pagos;
import com.pagos.pagoservice.repository.PagosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePagosService {
    
    private final PagosRepository repository;

    public Pagos reembolsarPago(String id) {
        Pagos pagoExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede reembolsar. Pago no encontrado con ID: " + id));

        // Validación de calidad (Idempotencia)
        if ("REEMBOLSADO".equalsIgnoreCase(pagoExistente.getStatus())) {
            throw new RuntimeException("Este pago ya se encuentra en estado REEMBOLSADO");
        }

        // Aplicamos el cambio de estado
        pagoExistente.setStatus("REEMBOLSADO");

        return repository.save(pagoExistente);
    }
}
