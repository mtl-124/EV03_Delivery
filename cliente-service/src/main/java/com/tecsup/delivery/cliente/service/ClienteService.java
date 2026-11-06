package com.tecsup.delivery.cliente.service;

import com.tecsup.delivery.cliente.dto.ClienteRequest;
import com.tecsup.delivery.cliente.dto.ClienteResponse;
import java.util.List;

public interface ClienteService {

    List<ClienteResponse> listar();

    ClienteResponse obtener(Long id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    void eliminar(Long id);
}
