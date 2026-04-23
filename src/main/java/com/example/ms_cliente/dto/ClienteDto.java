package com.example.ms_cliente.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDto {

    private Long id;

    @NotBlank(message = "DNI o RUC es obligatorio")
    private String dniOrRuc;


    private String razonSocialONombre;

    @NotBlank(message = "Dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "Teléfono es obligatorio")
    private String telefono;
}