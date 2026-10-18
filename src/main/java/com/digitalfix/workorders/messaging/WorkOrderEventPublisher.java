package com.digitalfix.workorders.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.digitalfix.workorders.config.RabbitMQConfig;
import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.messaging.dto.WorkOrderMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAssigned(WorkOrder wo) {
        String eventId = UUID.randomUUID().toString();
        publishAfterCommit(RabbitMQConfig.TOPIC_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY,
                toMessage(wo, eventId, "ORDEN_ASIGNADA"));
        publishAfterCommit(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.DISPATCH_ROUTING_KEY,
                toMessage(wo, eventId, "ORDEN_ASIGNADA"));
    }

    public void publishClosed(WorkOrder wo) {
        String eventId = UUID.randomUUID().toString();
        publishAfterCommit(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.REPORT_ROUTING_KEY,
                toMessage(wo, eventId, "ORDEN_CERRADA"));
    }

    private void publishAfterCommit(String exchange, String routingKey, WorkOrderMessage message) {
        Runnable publish = () -> {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Evento RabbitMQ publicado: eventId={} orderId={} tipo={} exchange={} routingKey={}",
                    message.eventId(), message.orderId(), message.tipo(), exchange, routingKey);
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    private WorkOrderMessage toMessage(WorkOrder wo, String eventId, String tipo) {
        return new WorkOrderMessage(
                eventId,
                wo.getId(),
                tipo,
                OffsetDateTime.now().toString(),
                wo.getClienteEmail(),
                wo.getServicio(),
                wo.getDescripcion(),
                wo.getEstado() != null ? wo.getEstado().name() : null,
                wo.getTecnicoAsignado(),
                wo.getRepuestoId());
    }
}
