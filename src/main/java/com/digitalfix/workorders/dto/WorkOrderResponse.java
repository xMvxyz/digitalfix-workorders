package com.digitalfix.workorders.dto;

import com.digitalfix.workorders.domain.WorkOrderStatus;
import java.time.LocalDateTime;

public record WorkOrderResponse(
        Long id,
        String clienteEmail,
        String servicio,
        String descripcion,
        WorkOrderStatus estado,
        String tecnicoAsignado,
        Long repuestoId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
