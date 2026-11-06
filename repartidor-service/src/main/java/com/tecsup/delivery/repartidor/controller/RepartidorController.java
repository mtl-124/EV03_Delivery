package com.tecsup.delivery.repartidor.controller;

import com.tecsup.delivery.repartidor.dto.ActivoRequest;
import com.tecsup.delivery.repartidor.dto.RepartidorRequest;
import com.tecsup.delivery.repartidor.dto.RepartidorResponse;
import com.tecsup.delivery.repartidor.service.RepartidorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repartidores")
public class RepartidorController {

    private final RepartidorService service;

    public RepartidorController(RepartidorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RepartidorResponse> crear(@Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    public List<RepartidorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RepartidorResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public RepartidorResponse actualizar(@PathVariable Long id, @Valid @RequestBody RepartidorRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/activo")
    public RepartidorResponse cambiarActivo(@PathVariable Long id, @Valid @RequestBody ActivoRequest request) {
        return service.cambiarActivo(id, request.activo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
