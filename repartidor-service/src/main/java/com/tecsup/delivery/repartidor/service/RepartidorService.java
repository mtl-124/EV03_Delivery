package com.tecsup.delivery.repartidor.service;

import com.tecsup.delivery.repartidor.dto.RepartidorRequest;
import com.tecsup.delivery.repartidor.dto.RepartidorResponse;
import java.util.List;

public interface RepartidorService {

    List<RepartidorResponse> listar();

    RepartidorResponse obtener(Long id);

    RepartidorResponse crear(RepartidorRequest request);

    RepartidorResponse actualizar(Long id, RepartidorRequest request);

    RepartidorResponse cambiarActivo(Long id, Boolean activo);

    void eliminar(Long id);
}
