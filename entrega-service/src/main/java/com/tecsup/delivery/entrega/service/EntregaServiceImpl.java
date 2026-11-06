package com.tecsup.delivery.entrega.service;

import com.tecsup.delivery.entrega.client.ClienteClient;
import com.tecsup.delivery.entrega.client.ClienteDto;
import com.tecsup.delivery.entrega.client.RepartidorClient;
import com.tecsup.delivery.entrega.client.RepartidorDto;
import com.tecsup.delivery.entrega.dto.EntregaRequest;
import com.tecsup.delivery.entrega.dto.EntregaResponse;
import com.tecsup.delivery.entrega.exception.ConflictoException;
import com.tecsup.delivery.entrega.exception.RecursoNoEncontradoException;
import com.tecsup.delivery.entrega.exception.ServicioNoDisponibleException;
import com.tecsup.delivery.entrega.model.entity.Entrega;
import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import com.tecsup.delivery.entrega.repository.EntregaRepository;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository repository;
    private final ClienteClient clienteClient;
    private final RepartidorClient repartidorClient;

    public EntregaServiceImpl(EntregaRepository repository, ClienteClient clienteClient, RepartidorClient repartidorClient) {
        this.repository = repository;
        this.clienteClient = clienteClient;
        this.repartidorClient = repartidorClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntregaResponse> listar(EstadoEntrega estado) {
        List<Entrega> entregas = estado == null ? repository.findAll() : repository.findByEstado(estado);
        return entregas.stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EntregaResponse obtener(Long id) {
        return convertir(buscar(id));
    }

    @Override
    @Transactional
    public EntregaResponse crear(EntregaRequest request) {
        obtenerCliente(request.idCliente());
        RepartidorDto repartidor = obtenerRepartidor(request.idRepartidor());
        if (!Boolean.TRUE.equals(repartidor.activo())) {
            throw new ConflictoException("El repartidor no está activo");
        }
        Entrega entrega = new Entrega();
        entrega.setIdCliente(request.idCliente());
        entrega.setIdRepartidor(request.idRepartidor());
        entrega.setDireccion(request.direccion());
        entrega.setCosto(request.costo());
        entrega.setFecha(LocalDateTime.now());
        entrega.setEstado(EstadoEntrega.PENDIENTE);
        return convertir(repository.save(entrega));
    }

    @Override
    @Transactional
    public EntregaResponse actualizarEstado(Long id, EstadoEntrega estado) {
        Entrega entrega = buscar(id);
        validarTransicion(entrega.getEstado(), estado);
        entrega.setEstado(estado);
        return convertir(repository.save(entrega));
    }

    private void validarTransicion(EstadoEntrega actual, EstadoEntrega nuevo) {
        boolean permitida = (actual == EstadoEntrega.PENDIENTE && nuevo == EstadoEntrega.EN_CAMINO)
                || (actual == EstadoEntrega.EN_CAMINO && nuevo == EstadoEntrega.ENTREGADA);
        if (!permitida) {
            throw new ConflictoException("Transición no permitida de " + actual + " a " + nuevo);
        }
    }

    private ClienteDto obtenerCliente(Long id) {
        try {
            return clienteClient.obtener(id);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id " + id);
        } catch (FeignException | IllegalStateException ex) {
            throw new ServicioNoDisponibleException("Servicio de clientes no disponible");
        }
    }

    private RepartidorDto obtenerRepartidor(Long id) {
        try {
            return repartidorClient.obtener(id);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNoEncontradoException("Repartidor no encontrado con id " + id);
        } catch (FeignException | IllegalStateException ex) {
            throw new ServicioNoDisponibleException("Servicio de repartidores no disponible");
        }
    }

    private Entrega buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrega no encontrada con id " + id));
    }

    private EntregaResponse convertir(Entrega entrega) {
        return new EntregaResponse(
                entrega.getId(),
                entrega.getIdCliente(),
                entrega.getIdRepartidor(),
                entrega.getDireccion(),
                entrega.getFecha(),
                entrega.getCosto(),
                entrega.getEstado());
    }
}
