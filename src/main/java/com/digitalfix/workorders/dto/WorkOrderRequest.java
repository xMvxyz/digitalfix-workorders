package com.digitalfix.workorders.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkOrderRequest(
        @NotBlank @Email String clienteEmail,
        @NotBlank @Size(max = 150) String servicio,
        @Size(max = 2000) String descripcion,
        Long repuestoId
) {}
