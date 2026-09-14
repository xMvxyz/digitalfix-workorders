package com.digitalfix.workorders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByClienteEmail(String email);
    List<WorkOrder> findByEstado(WorkOrderStatus estado);
    List<WorkOrder> findByTecnicoAsignado(String tecnico);
}
