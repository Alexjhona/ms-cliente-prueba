package com.example.ms_cliente.feign;

import com.example.ms_cliente.dto.DniResponse;
import com.example.ms_cliente.dto.RucResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "sunatClient", url = "https://api.apis.net.pe/v1")
public interface SunatClient {

    @GetMapping("/dni")
    DniResponse obtenerInfoDni(@RequestParam("numero") String numero);

    @GetMapping("/ruc")
    RucResponse obtenerInfoRuc(@RequestParam("numero") String numero);
}