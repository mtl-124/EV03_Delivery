package com.tecsup.delivery.entrega.repository;

import com.tecsup.delivery.entrega.model.entity.Entrega;
import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByEstado(EstadoEntrega estado);
}
