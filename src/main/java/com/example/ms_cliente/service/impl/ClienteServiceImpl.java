package com.example.ms_cliente.service.impl;

import com.example.ms_cliente.dto.ClienteDto;
import com.example.ms_cliente.entity.Cliente;
import com.example.ms_cliente.feign.SunatClient;
import com.example.ms_cliente.dto.DniResponse;
import com.example.ms_cliente.dto.RucResponse;
import com.example.ms_cliente.repository.ClienteRepository;
import com.example.ms_cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final SunatClient sunatClient;

    @Override
    public ClienteDto crearCliente(ClienteDto clienteDto) {
        String razonSocial = clienteDto.getRazonSocialONombre();

        // Si es DNI (8 dígitos), consultamos SUNAT y tomamos solo el nombre
        if (clienteDto.getDniOrRuc().length() == 8) {
            try {
                DniResponse respuesta = sunatClient.obtenerInfoDni(clienteDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Si falla, mantenemos el valor enviado
            }
        }
        // Si es RUC (11 dígitos), consultamos SUNAT y tomamos el nombre
        else if (clienteDto.getDniOrRuc().length() == 11) {
            try {
                RucResponse respuesta = sunatClient.obtenerInfoRuc(clienteDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Si falla, mantenemos el valor enviado
            }
        }

        clienteDto.setRazonSocialONombre(razonSocial);

        if (clienteRepository.existsByDniOrRuc(clienteDto.getDniOrRuc())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI o RUC");
        }

        Cliente entidad = Cliente.builder()
                .dniOrRuc(clienteDto.getDniOrRuc())
                .razonSocialONombre(clienteDto.getRazonSocialONombre())
                .direccion(clienteDto.getDireccion())
                .telefono(clienteDto.getTelefono())
                .build();

        Cliente guardado = clienteRepository.save(entidad);
        return mapToDto(guardado);
    }

    @Override
    public ClienteDto obtenerCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        return mapToDto(cliente);
    }

    @Override
    public List<ClienteDto> listarClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDto actualizarCliente(Long id, ClienteDto clienteDto) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));

        String razonSocial = clienteDto.getRazonSocialONombre();

        // Siempre consultamos SUNAT al editar, si es DNI o RUC
        if (clienteDto.getDniOrRuc().length() == 8) {
            try {
                DniResponse respuesta = sunatClient.obtenerInfoDni(clienteDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Mantener valor enviado
            }
        } else if (clienteDto.getDniOrRuc().length() == 11) {
            try {
                RucResponse respuesta = sunatClient.obtenerInfoRuc(clienteDto.getDniOrRuc());
                razonSocial = respuesta.getNombre();
            } catch (Exception ex) {
                // Mantener valor enviado
            }
        }

        clienteDto.setRazonSocialONombre(razonSocial);

        if (!existente.getDniOrRuc().equals(clienteDto.getDniOrRuc())
                && clienteRepository.existsByDniOrRuc(clienteDto.getDniOrRuc())) {
            throw new IllegalArgumentException("Ya existe otro cliente con ese DNI o RUC");
        }

        existente.setDniOrRuc(clienteDto.getDniOrRuc());
        existente.setRazonSocialONombre(clienteDto.getRazonSocialONombre());
        existente.setDireccion(clienteDto.getDireccion());
        existente.setTelefono(clienteDto.getTelefono());
        Cliente actualizado = clienteRepository.save(existente);
        return mapToDto(actualizado);
    }

    @Override
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe cliente con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteDto mapToDto(Cliente entidad) {
        return ClienteDto.builder()
                .id(entidad.getId())
                .dniOrRuc(entidad.getDniOrRuc())
                .razonSocialONombre(entidad.getRazonSocialONombre())
                .direccion(entidad.getDireccion())
                .telefono(entidad.getTelefono())
                .build();
    }
}
