package com.tecsup.delivery.entrega.service;

import com.tecsup.delivery.entrega.dto.EntregaRequest;
import com.tecsup.delivery.entrega.dto.EntregaResponse;
import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import java.util.List;

public interface EntregaService {

    List<EntregaResponse> listar(EstadoEntrega estado);

    EntregaResponse obtener(Long id);

    EntregaResponse crear(EntregaRequest request);

    EntregaResponse actualizarEstado(Long id, EstadoEntrega estado);
}
