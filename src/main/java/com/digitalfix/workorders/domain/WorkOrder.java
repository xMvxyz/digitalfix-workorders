package com.digitalfix.workorders.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String clienteEmail;

    @NotBlank
    private String servicio;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkOrderStatus estado = WorkOrderStatus.CREADA;

    private String tecnicoAsignado;

    private Long repuestoId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) estado = WorkOrderStatus.CREADA;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
