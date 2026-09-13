package com.digitalfix.workorders.controller;

import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.dto.WorkOrderRequest;
import com.digitalfix.workorders.dto.WorkOrderResponse;
import com.digitalfix.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService service;

    @GetMapping
    public List<WorkOrderResponse> list(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        boolean isPrivileged = "Admin".equalsIgnoreCase(role) || "Supervisor".equalsIgnoreCase(role);
        String safeEmail = email != null ? email : "anon@digitalfix.cl";
        return service.findAll(safeEmail, isPrivileged);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("estado");
        String tecnico = body.get("tecnico");
        WorkOrderStatus target = WorkOrderStatus.valueOf(statusStr);
        return service.changeStatus(id, target, tecnico);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(Exception ex) {
        return Map.of("error", ex.getMessage());
    }
}
