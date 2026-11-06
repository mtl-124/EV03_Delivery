package com.tecsup.delivery.repartidor.service;

import com.tecsup.delivery.repartidor.dto.RepartidorRequest;
import com.tecsup.delivery.repartidor.dto.RepartidorResponse;
import com.tecsup.delivery.repartidor.exception.RecursoNoEncontradoException;
import com.tecsup.delivery.repartidor.model.entity.Repartidor;
import com.tecsup.delivery.repartidor.repository.RepartidorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepartidorServiceImpl implements RepartidorService {

    private final RepartidorRepository repository;

    public RepartidorServiceImpl(RepartidorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepartidorResponse> listar() {
        return repository.findAll().stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RepartidorResponse obtener(Long id) {
        return convertir(buscar(id));
    }

    @Override
    @Transactional
    public RepartidorResponse crear(RepartidorRequest request) {
        Repartidor repartidor = new Repartidor();
        aplicar(repartidor, request);
        repartidor.setActivo(request.activo() == null ? Boolean.TRUE : request.activo());
        return convertir(repository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponse actualizar(Long id, RepartidorRequest request) {
        Repartidor repartidor = buscar(id);
        aplicar(repartidor, request);
        if (request.activo() != null) {
            repartidor.setActivo(request.activo());
        }
        return convertir(repository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponse cambiarActivo(Long id, Boolean activo) {
        Repartidor repartidor = buscar(id);
        repartidor.setActivo(activo);
        return convertir(repository.save(repartidor));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Repartidor repartidor = buscar(id);
        repository.delete(repartidor);
    }

    private Repartidor buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor no encontrado con id " + id));
    }

    private void aplicar(Repartidor repartidor, RepartidorRequest request) {
        repartidor.setNombre(request.nombre());
        repartidor.setApellido(request.apellido());
        repartidor.setTelefono(request.telefono());
        repartidor.setVehiculo(request.vehiculo());
    }

    private RepartidorResponse convertir(Repartidor repartidor) {
        return new RepartidorResponse(
                repartidor.getId(),
                repartidor.getNombre(),
                repartidor.getApellido(),
                repartidor.getTelefono(),
                repartidor.getVehiculo(),
                repartidor.getActivo());
    }
}
