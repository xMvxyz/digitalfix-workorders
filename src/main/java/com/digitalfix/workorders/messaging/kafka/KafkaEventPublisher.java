package com.digitalfix.workorders.messaging.kafka;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.messaging.kafka.dto.WorkOrderAuditEvent;
import com.digitalfix.workorders.messaging.kafka.dto.WorkOrderKafkaEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class KafkaEventPublisher {

    private static final String TOPIC_EVENTOS = "digitalfix-eventos";
    private static final String TOPIC_AUDITORIA = "digitalfix-auditoria";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderEvent(WorkOrder wo, String tipo) {
        WorkOrderKafkaEvent event = new WorkOrderKafkaEvent(
                UUID.randomUUID().toString(),
                wo.getId(),
                tipo,
                OffsetDateTime.now(),
                wo.getClienteEmail(),
                wo.getServicio(),
                wo.getDescripcion(),
                wo.getEstado() != null ? wo.getEstado().name() : null,
                wo.getTecnicoAsignado(),
                wo.getRepuestoId()
        );

        publishAfterCommit(TOPIC_EVENTOS, wo.getId().toString(), event);
    }

    public void publishAuditEvent(
            WorkOrder wo,
            String accion,
            WorkOrderStatus estadoAnterior,
            String usuario
    ) {
        WorkOrderAuditEvent event = new WorkOrderAuditEvent(
                UUID.randomUUID().toString(),
                wo.getId(),
                accion,
                estadoAnterior != null ? estadoAnterior.name() : null,
                wo.getEstado() != null ? wo.getEstado().name() : null,
                usuario,
                OffsetDateTime.now()
        );

        publishAfterCommit(TOPIC_AUDITORIA, wo.getId().toString(), event);
    }

    private void publishAfterCommit(String topic, String key, Object event) {
        Runnable publish = () -> kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "Error publicando evento Kafka. topic={} key={}",
                                topic, key, ex
                        );
                        return;
                    }

                    log.info(
                            "Evento Kafka publicado. topic={} partition={} offset={} key={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key
                    );
                });

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publish.run();
                        }
                    }
            );
        } else {
            publish.run();
        }
    }
}
