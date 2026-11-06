package com.tecsup.delivery.cliente.service;

import com.tecsup.delivery.cliente.dto.ClienteRequest;
import com.tecsup.delivery.cliente.dto.ClienteResponse;
import com.tecsup.delivery.cliente.exception.ConflictoException;
import com.tecsup.delivery.cliente.exception.RecursoNoEncontradoException;
import com.tecsup.delivery.cliente.model.entity.Cliente;
import com.tecsup.delivery.cliente.repository.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return convertir(buscar(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictoException("El email ya está registrado");
        }
        Cliente cliente = new Cliente();
        aplicar(cliente, request);
        return convertir(repository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictoException("El email ya está registrado");
        }
        aplicar(cliente, request);
        return convertir(repository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscar(id);
        repository.delete(cliente);
    }

    private Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id " + id));
    }

    private void aplicar(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
    }

    private ClienteResponse convertir(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion());
    }
}
