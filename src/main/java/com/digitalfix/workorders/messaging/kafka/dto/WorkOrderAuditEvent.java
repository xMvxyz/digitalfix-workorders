package com.digitalfix.workorders.messaging.kafka.dto;

import java.time.OffsetDateTime;

public record WorkOrderAuditEvent(
    String eventId,
        Long orderId,
        String accion,
        String estadoAnterior,
        String estadoNuevo,
        String usuario,
        OffsetDateTime fecha
) {}
