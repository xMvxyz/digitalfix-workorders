package com.digitalfix.workorders.service;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.dto.WorkOrderRequest;
import com.digitalfix.workorders.dto.WorkOrderResponse;
import com.digitalfix.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository repository;

    private static final Map<WorkOrderStatus, WorkOrderStatus> NEXT = Map.of(
            WorkOrderStatus.CREADA, WorkOrderStatus.ASIGNADA,
            WorkOrderStatus.ASIGNADA, WorkOrderStatus.EN_DESPLAZAMIENTO,
            WorkOrderStatus.EN_DESPLAZAMIENTO, WorkOrderStatus.EN_EJECUCION,
            WorkOrderStatus.EN_EJECUCION, WorkOrderStatus.CERRADA
    );

    public List<WorkOrderResponse> findAll(String email, boolean isAdminOrSupervisor) {
        List<WorkOrder> list = isAdminOrSupervisor
                ? repository.findAll()
                : repository.findByClienteEmail(email);
        return list.stream().map(this::toResponse).toList();
    }

    public WorkOrderResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest req) {
        WorkOrder wo = WorkOrder.builder()
                .clienteEmail(req.clienteEmail())
                .servicio(req.servicio())
                .descripcion(req.descripcion())
                .repuestoId(req.repuestoId())
                .estado(WorkOrderStatus.CREADA)
                .build();
        return toResponse(repository.save(wo));
    }

    @Transactional
    public WorkOrderResponse changeStatus(Long id, WorkOrderStatus target, String tecnico) {
        WorkOrder wo = getOrThrow(id);

        if (target == WorkOrderStatus.EN_EJECUCION && wo.getEstado() != WorkOrderStatus.EN_DESPLAZAMIENTO) {
            throw new IllegalStateException("No se puede pasar a EN_EJECUCION sin estar en EN_DESPLAZAMIENTO (regla: debe estar ASIGNADA antes)");
        }
        if (wo.getEstado() == WorkOrderStatus.CREADA && target != WorkOrderStatus.ASIGNADA && target != WorkOrderStatus.CANCELADA) {
            throw new IllegalStateException("Desde CREADA solo se puede pasar a ASIGNADA o CANCELADA");
        }

        WorkOrderStatus expected = NEXT.get(wo.getEstado());
        if (target != WorkOrderStatus.CANCELADA && target != expected) {
            if (wo.getEstado() == WorkOrderStatus.CERRADA || wo.getEstado() == WorkOrderStatus.CANCELADA) {
                throw new IllegalStateException("Orden ya finalizada");
            }
            throw new IllegalStateException("Transicion no permitida: " + wo.getEstado() + " -> " + target + ". Esperado: " + expected);
        }

        if (target == WorkOrderStatus.ASIGNADA) {
            if (tecnico == null || tecnico.isBlank()) throw new IllegalArgumentException("Tecnico requerido al asignar");
            wo.setTecnicoAsignado(tecnico);
        }

        wo.setEstado(target);
        return toResponse(repository.save(wo));
    }

    private WorkOrder getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
    }

    private WorkOrderResponse toResponse(WorkOrder wo) {
        return new WorkOrderResponse(
                wo.getId(), wo.getClienteEmail(), wo.getServicio(), wo.getDescripcion(),
                wo.getEstado(), wo.getTecnicoAsignado(), wo.getRepuestoId(),
                wo.getCreatedAt(), wo.getUpdatedAt()
        );
    }
}
