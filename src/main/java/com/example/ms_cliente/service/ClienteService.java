package com.example.ms_cliente.service;
import com.example.ms_cliente.dto.ClienteDto;
import java.util.List;

public interface ClienteService {
    ClienteDto crearCliente(ClienteDto clienteDto);
    ClienteDto obtenerCliente(Long id);
    List<ClienteDto> listarClientes();
    ClienteDto actualizarCliente(Long id, ClienteDto clienteDto);
    void eliminarCliente(Long id);
}