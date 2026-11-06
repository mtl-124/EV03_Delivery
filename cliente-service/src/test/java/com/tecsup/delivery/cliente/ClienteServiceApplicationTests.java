package com.tecsup.delivery.cliente;

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
class ClienteServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void crearClienteValidoResponde201() throws Exception {
        String cuerpo = "{\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"email\":\"ana.test@correo.com\","
                + "\"telefono\":\"987654321\",\"direccion\":\"Av. Lima 123\"}";
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("ana.test@correo.com"));
    }

    @Test
    void emailDuplicadoResponde409() throws Exception {
        String cuerpo = "{\"nombre\":\"Luis\",\"apellido\":\"Diaz\",\"email\":\"luis.dup@correo.com\","
                + "\"telefono\":\"987654321\",\"direccion\":\"Av. Lima 123\"}";
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void datosInvalidosResponde400ConDetalle() throws Exception {
        String cuerpo = "{\"nombre\":\"\",\"apellido\":\"Perez\",\"email\":\"no-es-email\","
                + "\"telefono\":\"12\",\"direccion\":\"\"}";
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void clienteInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/clientes/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void idNoNumericoResponde400() throws Exception {
        mockMvc.perform(get("/api/clientes/abc"))
                .andExpect(status().isBadRequest());
    }
}
