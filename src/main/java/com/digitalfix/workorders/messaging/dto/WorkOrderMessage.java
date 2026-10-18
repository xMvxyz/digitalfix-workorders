package com.digitalfix.workorders.messaging.dto;

public record WorkOrderMessage(
        String eventId,
        Long orderId,
        String tipo,
        String fecha,
        String clienteEmail,
        String servicio,
        String descripcion,
        String estado,
        String tecnicoAsignado,
        Long repuestoId
) {}
