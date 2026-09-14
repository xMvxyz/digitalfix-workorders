package com.digitalfix.workorders.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.dto.WorkOrderRequest;
import com.digitalfix.workorders.dto.WorkOrderResponse;
import com.digitalfix.workorders.exception.InvalidStateTransitionException;
import com.digitalfix.workorders.exception.ResourceNotFoundException;
import com.digitalfix.workorders.exception.WorkOrderAccessDeniedException;
import com.digitalfix.workorders.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;

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

    private static final Set<WorkOrderStatus> TERMINAL = Set.of(WorkOrderStatus.CERRADA, WorkOrderStatus.CANCELADA);

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAll(String email, boolean isAdminOrSupervisor) {
        List<WorkOrder> list = isAdminOrSupervisor
                ? repository.findAll()
                : repository.findByClienteEmail(email);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findById(Long id, String email, String role) {
        WorkOrder workOrder = getOrThrow(id);
        assertCanAccess(workOrder, email, role);
        return toResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest req, String email, String role) {
        if (!isPrivileged(role) && !req.clienteEmail().equalsIgnoreCase(email)) {
            throw new WorkOrderAccessDeniedException("El cliente solo puede crear órdenes a su nombre");
        }
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
    public WorkOrderResponse changeStatus(Long id, WorkOrderStatus target, String tecnico, String email, String role) {
        WorkOrder wo = getOrThrow(id);
        assertCanAccess(wo, email, role);
        WorkOrderStatus current = wo.getEstado();

        if (TERMINAL.contains(current)) {
            throw new InvalidStateTransitionException("Orden ya finalizada en estado " + current + ", no admite transiciones");
        }

        if (target == WorkOrderStatus.CANCELADA) {
            // CANCELADA permitida desde cualquier estado no terminal
            wo.setEstado(target);
            return toResponse(repository.save(wo));
        }

        if (current == WorkOrderStatus.CREADA && target != WorkOrderStatus.ASIGNADA) {
            throw new InvalidStateTransitionException("Desde CREADA solo se puede pasar a ASIGNADA o CANCELADA");
        }

        WorkOrderStatus expected = NEXT.get(current);
        if (expected == null || target != expected) {
            throw new InvalidStateTransitionException(
                    "Transicion no permitida: " + current + " -> " + target + ". Esperado: " + expected);
        }

        if (target == WorkOrderStatus.ASIGNADA) {
            if (tecnico == null || tecnico.isBlank()) throw new IllegalArgumentException("Tecnico requerido al asignar");
            wo.setTecnicoAsignado(tecnico);
        }

        wo.setEstado(target);
        return toResponse(repository.save(wo));
    }

    @Transactional
    public void delete(Long id, String email, String role) {
        WorkOrder wo = getOrThrow(id);
        if (!isPrivileged(role)) {
            throw new WorkOrderAccessDeniedException("Solo Supervisor o Admin pueden eliminar órdenes");
        }
        if (wo.getEstado() == WorkOrderStatus.CERRADA) {
            throw new InvalidStateTransitionException("No se puede eliminar una orden CERRADA");
        }
        repository.delete(wo);
    }

    private WorkOrder getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    private void assertCanAccess(WorkOrder workOrder, String email, String role) {
        if (!isPrivileged(role) && !workOrder.getClienteEmail().equalsIgnoreCase(email)) {
            throw new WorkOrderAccessDeniedException("El cliente no tiene permisos sobre esta orden");
        }
    }

    private boolean isPrivileged(String role) {
        return "Admin".equalsIgnoreCase(role) || "Supervisor".equalsIgnoreCase(role);
    }

    private WorkOrderResponse toResponse(WorkOrder wo) {
        return new WorkOrderResponse(
                wo.getId(), wo.getClienteEmail(), wo.getServicio(), wo.getDescripcion(),
                wo.getEstado(), wo.getTecnicoAsignado(), wo.getRepuestoId(),
                wo.getCreatedAt(), wo.getUpdatedAt()
        );
    }
}
