package com.pagos.pagoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String orderId;
    private String emailCliente;
    private String amount;
    private boolean fullyPaid;
}
