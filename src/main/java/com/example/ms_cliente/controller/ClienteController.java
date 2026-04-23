package com.example.ms_cliente.controller;


import com.example.ms_cliente.dto.ClienteDto;
import com.example.ms_cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Validated
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteDto> crear(@Valid @RequestBody ClienteDto clienteDto) {
        ClienteDto creado = clienteService.crearCliente(clienteDto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> obtener(@PathVariable Long id) {
        ClienteDto dto = clienteService.obtenerCliente(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ClienteDto>> listar() {
        List<ClienteDto> lista = clienteService.listarClientes();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDto clienteDto) {
        ClienteDto actualizado = clienteService.actualizarCliente(id, clienteDto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}