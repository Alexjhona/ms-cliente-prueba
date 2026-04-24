package com.example.ms_cliente.controller;

import com.example.ms_cliente.dto.ClienteDto;
import com.example.ms_cliente.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerTest {

    private MockMvc mockMvc;
    private ClienteService clienteService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clienteService = Mockito.mock(ClienteService.class);
        ClienteController clienteController = new ClienteController(clienteService);
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/clientes - crear cliente")
    void crearCliente_DebeRetornarCreated() throws Exception {
        ClienteDto entrada = crearClienteDto(null, "12345678", "Cliente Prueba");
        ClienteDto salida = crearClienteDto(1L, "12345678", "Cliente Prueba");

        when(clienteService.crearCliente(Mockito.any(ClienteDto.class))).thenReturn(salida);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Cliente Prueba"))
                .andExpect(jsonPath("$.direccion").value("Av. Lima 123"))
                .andExpect(jsonPath("$.telefono").value("999999999"));

        verify(clienteService).crearCliente(Mockito.any(ClienteDto.class));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} - obtener cliente")
    void obtenerCliente_DebeRetornarOk() throws Exception {
        ClienteDto salida = crearClienteDto(1L, "12345678", "Cliente Prueba");

        when(clienteService.obtenerCliente(1L)).thenReturn(salida);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Cliente Prueba"))
                .andExpect(jsonPath("$.direccion").value("Av. Lima 123"))
                .andExpect(jsonPath("$.telefono").value("999999999"));

        verify(clienteService).obtenerCliente(1L);
    }

    @Test
    @DisplayName("GET /api/clientes - listar clientes")
    void listarClientes_DebeRetornarOk() throws Exception {
        ClienteDto cliente1 = crearClienteDto(1L, "12345678", "Cliente Uno");
        ClienteDto cliente2 = crearClienteDto(2L, "20123456789", "Cliente Dos");

        when(clienteService.listarClientes()).thenReturn(List.of(cliente1, cliente2));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].dniOrRuc").value("12345678"))
                .andExpect(jsonPath("$[0].razonSocialONombre").value("Cliente Uno"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].dniOrRuc").value("20123456789"))
                .andExpect(jsonPath("$[1].razonSocialONombre").value("Cliente Dos"));

        verify(clienteService).listarClientes();
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} - actualizar cliente")
    void actualizarCliente_DebeRetornarOk() throws Exception {
        ClienteDto entrada = crearClienteDto(null, "87654321", "Cliente Actualizado");
        ClienteDto salida = crearClienteDto(1L, "87654321", "Cliente Actualizado");

        when(clienteService.actualizarCliente(Mockito.eq(1L), Mockito.any(ClienteDto.class))).thenReturn(salida);

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dniOrRuc").value("87654321"))
                .andExpect(jsonPath("$.razonSocialONombre").value("Cliente Actualizado"))
                .andExpect(jsonPath("$.direccion").value("Av. Lima 123"))
                .andExpect(jsonPath("$.telefono").value("999999999"));

        verify(clienteService).actualizarCliente(Mockito.eq(1L), Mockito.any(ClienteDto.class));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} - eliminar cliente")
    void eliminarCliente_DebeRetornarNoContent() throws Exception {
        doNothing().when(clienteService).eliminarCliente(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService).eliminarCliente(1L);
    }

    private ClienteDto crearClienteDto(Long id, String dniOrRuc, String razonSocialONombre) {
        return ClienteDto.builder()
                .id(id)
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion("Av. Lima 123")
                .telefono("999999999")
                .build();
    }
}