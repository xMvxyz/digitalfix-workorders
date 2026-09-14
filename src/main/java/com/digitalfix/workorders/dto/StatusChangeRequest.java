package com.digitalfix.workorders.dto;

import com.digitalfix.workorders.domain.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull WorkOrderStatus estado,
        String tecnico
) {}
