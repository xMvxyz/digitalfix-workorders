package com.digitalfix.workorders.messaging.kafka.dto;

import java.time.OffsetDateTime;

public record WorkOrderKafkaEvent(
    String eventId,
        Long orderId,
        String tipo,
        OffsetDateTime fecha,
        String clienteEmail,
        String servicio,
        String descripcion,
        String estado,
        String tecnicoAsignado,
        Long repuestoId
) {}
