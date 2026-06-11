package com.queries.querie.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("emailCliente")
    private String emailCliente;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("fullyPaid")
    private boolean fullyPaid;
}
