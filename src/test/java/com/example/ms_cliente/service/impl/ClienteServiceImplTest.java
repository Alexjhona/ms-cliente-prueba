package com.example.ms_cliente.service.impl;

import com.example.ms_cliente.dto.ClienteDto;
import com.example.ms_cliente.dto.DniResponse;
import com.example.ms_cliente.dto.RucResponse;
import com.example.ms_cliente.entity.Cliente;
import com.example.ms_cliente.feign.SunatClient;
import com.example.ms_cliente.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SunatClient sunatClient;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    @DisplayName("Crear cliente con DNI - consulta SUNAT y guarda correctamente")
    void crearCliente_ConDni_GuardaCorrectamente() {
        ClienteDto entrada = crearClienteDto("12345678", "Nombre enviado");

        DniResponse dniResponse = new DniResponse();
        dniResponse.setDni("12345678");
        dniResponse.setNombre("Juan Perez");

        when(sunatClient.obtenerInfoDni("12345678")).thenReturn(dniResponse);
        when(clienteRepository.existsByDniOrRuc("12345678")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        ClienteDto resultado = clienteService.crearCliente(entrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Juan Perez");
        assertThat(resultado.getDireccion()).isEqualTo("Av. Lima 123");
        assertThat(resultado.getTelefono()).isEqualTo("999999999");

        verify(sunatClient).obtenerInfoDni("12345678");
        verify(clienteRepository).existsByDniOrRuc("12345678");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con RUC - consulta SUNAT y guarda correctamente")
    void crearCliente_ConRuc_GuardaCorrectamente() {
        ClienteDto entrada = crearClienteDto("20123456789", "Empresa enviada");

        RucResponse rucResponse = new RucResponse();
        rucResponse.setNombre("Empresa ABC SAC");

        when(sunatClient.obtenerInfoRuc("20123456789")).thenReturn(rucResponse);
        when(clienteRepository.existsByDniOrRuc("20123456789")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(2L);
            return cliente;
        });

        ClienteDto resultado = clienteService.crearCliente(entrada);

        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("20123456789");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Empresa ABC SAC");

        verify(sunatClient).obtenerInfoRuc("20123456789");
        verify(clienteRepository).existsByDniOrRuc("20123456789");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente - mantiene nombre enviado si SUNAT falla")
    void crearCliente_SunatFalla_MantieneNombreEnviado() {
        ClienteDto entrada = crearClienteDto("12345678", "Nombre Manual");

        when(sunatClient.obtenerInfoDni("12345678")).thenThrow(new RuntimeException("Error SUNAT"));
        when(clienteRepository.existsByDniOrRuc("12345678")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(3L);
            return cliente;
        });

        ClienteDto resultado = clienteService.crearCliente(entrada);

        assertThat(resultado.getId()).isEqualTo(3L);
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Nombre Manual");

        verify(sunatClient).obtenerInfoDni("12345678");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente - lanza excepción si DNI o RUC ya existe")
    void crearCliente_DniDuplicado_LanzaExcepcion() {
        ClienteDto entrada = crearClienteDto("12345678", "Cliente Duplicado");

        DniResponse dniResponse = new DniResponse();
        dniResponse.setNombre("Cliente Duplicado");

        when(sunatClient.obtenerInfoDni("12345678")).thenReturn(dniResponse);
        when(clienteRepository.existsByDniOrRuc("12345678")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.crearCliente(entrada)
        );

        assertThat(exception.getMessage()).contains("Ya existe un cliente");

        verify(clienteRepository).existsByDniOrRuc("12345678");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Obtener cliente - retorna cliente existente")
    void obtenerCliente_RetornaClienteExistente() {
        Cliente cliente = crearClienteEntidad(1L, "12345678", "Juan Perez");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteDto resultado = clienteService.obtenerCliente(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Juan Perez");

        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener cliente - lanza excepción si no existe")
    void obtenerCliente_NoExiste_LanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.obtenerCliente(99L)
        );

        assertThat(exception.getMessage()).contains("Cliente no encontrado");

        verify(clienteRepository).findById(99L);
    }

    @Test
    @DisplayName("Listar clientes - retorna lista")
    void listarClientes_RetornaLista() {
        Cliente cliente1 = crearClienteEntidad(1L, "12345678", "Juan Perez");
        Cliente cliente2 = crearClienteEntidad(2L, "20123456789", "Empresa ABC SAC");

        when(clienteRepository.findAll()).thenReturn(List.of(cliente1, cliente2));

        List<ClienteDto> resultado = clienteService.listarClientes();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);

        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Actualizar cliente - actualiza correctamente con DNI")
    void actualizarCliente_ConDni_ActualizaCorrectamente() {
        Cliente existente = crearClienteEntidad(1L, "12345678", "Nombre Antiguo");
        ClienteDto entrada = crearClienteDto("87654321", "Nombre Nuevo");

        DniResponse dniResponse = new DniResponse();
        dniResponse.setNombre("Nombre Desde SUNAT");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoDni("87654321")).thenReturn(dniResponse);
        when(clienteRepository.existsByDniOrRuc("87654321")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteDto resultado = clienteService.actualizarCliente(1L, entrada);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDniOrRuc()).isEqualTo("87654321");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Nombre Desde SUNAT");
        assertThat(resultado.getDireccion()).isEqualTo("Av. Lima 123");
        assertThat(resultado.getTelefono()).isEqualTo("999999999");

        verify(clienteRepository).findById(1L);
        verify(sunatClient).obtenerInfoDni("87654321");
        verify(clienteRepository).existsByDniOrRuc("87654321");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Actualizar cliente - actualiza correctamente con el mismo documento")
    void actualizarCliente_MismoDocumento_NoValidaDuplicado() {
        Cliente existente = crearClienteEntidad(1L, "12345678", "Nombre Antiguo");
        ClienteDto entrada = crearClienteDto("12345678", "Nombre Editado");

        DniResponse dniResponse = new DniResponse();
        dniResponse.setNombre("Nombre SUNAT Editado");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoDni("12345678")).thenReturn(dniResponse);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteDto resultado = clienteService.actualizarCliente(1L, entrada);

        assertThat(resultado.getDniOrRuc()).isEqualTo("12345678");
        assertThat(resultado.getRazonSocialONombre()).isEqualTo("Nombre SUNAT Editado");

        verify(clienteRepository, never()).existsByDniOrRuc("12345678");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Actualizar cliente - lanza excepción si no existe")
    void actualizarCliente_NoExiste_LanzaExcepcion() {
        ClienteDto entrada = crearClienteDto("12345678", "Cliente");

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.actualizarCliente(99L, entrada)
        );

        assertThat(exception.getMessage()).contains("Cliente no encontrado");

        verify(clienteRepository).findById(99L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Actualizar cliente - lanza excepción si nuevo documento ya existe")
    void actualizarCliente_DocumentoDuplicado_LanzaExcepcion() {
        Cliente existente = crearClienteEntidad(1L, "12345678", "Cliente Antiguo");
        ClienteDto entrada = crearClienteDto("87654321", "Cliente Nuevo");

        DniResponse dniResponse = new DniResponse();
        dniResponse.setNombre("Cliente Nuevo");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(sunatClient.obtenerInfoDni("87654321")).thenReturn(dniResponse);
        when(clienteRepository.existsByDniOrRuc("87654321")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.actualizarCliente(1L, entrada)
        );

        assertThat(exception.getMessage()).contains("Ya existe otro cliente");

        verify(clienteRepository).existsByDniOrRuc("87654321");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Eliminar cliente - elimina si existe")
    void eliminarCliente_Existe_EliminaCorrectamente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        clienteService.eliminarCliente(1L);

        verify(clienteRepository).existsById(1L);
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar cliente - lanza excepción si no existe")
    void eliminarCliente_NoExiste_LanzaExcepcion() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.eliminarCliente(99L)
        );

        assertThat(exception.getMessage()).contains("No existe cliente");

        verify(clienteRepository).existsById(99L);
        verify(clienteRepository, never()).deleteById(anyLong());
    }

    private ClienteDto crearClienteDto(String dniOrRuc, String razonSocialONombre) {
        return ClienteDto.builder()
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion("Av. Lima 123")
                .telefono("999999999")
                .build();
    }

    private Cliente crearClienteEntidad(Long id, String dniOrRuc, String razonSocialONombre) {
        return Cliente.builder()
                .id(id)
                .dniOrRuc(dniOrRuc)
                .razonSocialONombre(razonSocialONombre)
                .direccion("Av. Lima 123")
                .telefono("999999999")
                .build();
    }
}