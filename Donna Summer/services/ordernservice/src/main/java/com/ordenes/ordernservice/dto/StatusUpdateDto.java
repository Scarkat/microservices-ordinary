package com.ordenes.ordernservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateDto {
    @NotBlank(message = "El nuevo status no puede estar vacío")
    private String status;
}