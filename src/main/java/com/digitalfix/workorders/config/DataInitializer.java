package com.digitalfix.workorders.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.repository.WorkOrderRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(WorkOrderRepository workOrderRepo) {
        return args -> {
            if (workOrderRepo.count() == 0) {
                WorkOrder orden = new WorkOrder();
                // Ajusta los setters según tu entidad
                orden.setClienteEmail("cliente@digitalfix.cl");
                orden.setServicio("Reparacion de pantalla");
                orden.setDescripcion("Pantalla rota");
                orden.setEstado(WorkOrderStatus.CREADA); // Usa tu Enum o "CREADA"
                workOrderRepo.save(orden);
            }
            System.out.println("Base de datos de WorkOrders inicializada.");
        };
    }
}
