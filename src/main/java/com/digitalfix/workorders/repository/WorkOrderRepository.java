package com.digitalfix.workorders.repository;

import com.digitalfix.workorders.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByClienteEmail(String email);
}
