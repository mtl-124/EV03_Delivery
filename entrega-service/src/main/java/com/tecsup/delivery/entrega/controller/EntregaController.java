package com.tecsup.delivery.entrega.controller;

import com.tecsup.delivery.entrega.dto.EntregaRequest;
import com.tecsup.delivery.entrega.dto.EntregaResponse;
import com.tecsup.delivery.entrega.dto.EstadoRequest;
import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import com.tecsup.delivery.entrega.service.EntregaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService service;

    public EntregaController(EntregaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntregaResponse> crear(@Valid @RequestBody EntregaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    public List<EntregaResponse> listar(@RequestParam(required = false) EstadoEntrega estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public EntregaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    public EntregaResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return service.actualizarEstado(id, request.estado());
    }
}
