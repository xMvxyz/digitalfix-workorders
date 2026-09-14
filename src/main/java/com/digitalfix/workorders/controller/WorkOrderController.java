package com.digitalfix.workorders.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.digitalfix.workorders.dto.StatusChangeRequest;
import com.digitalfix.workorders.dto.WorkOrderRequest;
import com.digitalfix.workorders.dto.WorkOrderResponse;
import com.digitalfix.workorders.service.WorkOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public WorkOrderResponse get(@PathVariable Long id,
                                 @RequestHeader(value = "X-User-Email", required = false) String email,
                                 @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.findById(id, email, role);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest req,
                                    @RequestHeader(value = "X-User-Email", required = false) String email,
                                    @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.create(req, email, role);
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeRequest body,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.changeStatus(id, body.estado(), body.tecnico(), email, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader(value = "X-User-Email", required = false) String email,
                                       @RequestHeader(value = "X-User-Role", required = false) String role) {
        service.delete(id, email, role);
        return ResponseEntity.noContent().build();
    }
}
