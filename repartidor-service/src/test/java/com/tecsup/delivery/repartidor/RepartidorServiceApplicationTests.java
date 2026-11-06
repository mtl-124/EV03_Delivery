package com.tecsup.delivery.repartidor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RepartidorServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void crearSinActivoQuedaActivo() throws Exception {
        String cuerpo = "{\"nombre\":\"Carlos\",\"apellido\":\"Rojas\",\"telefono\":\"999111222\",\"vehiculo\":\"MOTO\"}";
        mockMvc.perform(post("/api/repartidores").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.vehiculo").value("MOTO"));
    }

    @Test
    void vehiculoInvalidoResponde400() throws Exception {
        String cuerpo = "{\"nombre\":\"Carlos\",\"apellido\":\"Rojas\",\"telefono\":\"999111222\",\"vehiculo\":\"AVION\"}";
        mockMvc.perform(post("/api/repartidores").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void repartidorInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/repartidores/999999"))
                .andExpect(status().isNotFound());
    }
}
