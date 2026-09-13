package com.digitalfix.workorders.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkOrderRequest(
        @NotBlank String clienteEmail,
        @NotBlank String servicio,
        String descripcion,
        Long repuestoId
) {}
