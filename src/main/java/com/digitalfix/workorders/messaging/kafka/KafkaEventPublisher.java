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
import tools.jackson.databind.ObjectMapper;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class KafkaEventPublisher {

    private static final String TOPIC_EVENTOS = "digitalfix-eventos";
    private static final String TOPIC_AUDITORIA = "digitalfix-auditoria";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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
        Runnable publish = () -> {
            try {
                String payload = objectMapper.writeValueAsString(event);

                kafkaTemplate.send(topic, key, payload)
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
            } catch (Exception ex) {
                log.error(
                        "Kafka no disponible; evento no publicado. topic={} key={}",
                        topic, key, ex
                );
            }
        };

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
