package com.tecsup.delivery.entrega;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tecsup.delivery.entrega.client.ClienteClient;
import com.tecsup.delivery.entrega.client.ClienteDto;
import com.tecsup.delivery.entrega.client.RepartidorClient;
import com.tecsup.delivery.entrega.client.RepartidorDto;
import com.tecsup.delivery.entrega.dto.EntregaRequest;
import com.tecsup.delivery.entrega.dto.EntregaResponse;
import com.tecsup.delivery.entrega.exception.ConflictoException;
import com.tecsup.delivery.entrega.model.entity.Entrega;
import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import com.tecsup.delivery.entrega.repository.EntregaRepository;
import com.tecsup.delivery.entrega.service.EntregaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EntregaServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteClient clienteClientMock;

    @MockitoBean
    private RepartidorClient repartidorClientMock;

    @Test
    void contextLoads() {
    }

    @Test
    void costoNegativoResponde400() throws Exception {
        String cuerpo = "{\"idCliente\":1,\"idRepartidor\":1,\"direccion\":\"Av. Lima 123\",\"costo\":-5}";
        mockMvc.perform(post("/api/entregas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void direccionVaciaResponde400() throws Exception {
        String cuerpo = "{\"idCliente\":1,\"idRepartidor\":1,\"direccion\":\"\",\"costo\":10}";
        mockMvc.perform(post("/api/entregas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transicionValidaPendienteAEnCamino() {
        EntregaRepository repository = mock(EntregaRepository.class);
        EntregaServiceImpl service = new EntregaServiceImpl(repository, mock(ClienteClient.class), mock(RepartidorClient.class));
        Entrega entrega = entregaConEstado(EstadoEntrega.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(entrega));
        when(repository.save(any(Entrega.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        EntregaResponse respuesta = service.actualizarEstado(1L, EstadoEntrega.EN_CAMINO);

        assertEquals(EstadoEntrega.EN_CAMINO, respuesta.estado());
    }

    @Test
    void transicionInvalidaLanzaConflicto() {
        EntregaRepository repository = mock(EntregaRepository.class);
        EntregaServiceImpl service = new EntregaServiceImpl(repository, mock(ClienteClient.class), mock(RepartidorClient.class));
        Entrega entrega = entregaConEstado(EstadoEntrega.ENTREGADA);
        when(repository.findById(1L)).thenReturn(Optional.of(entrega));

        assertThrows(ConflictoException.class, () -> service.actualizarEstado(1L, EstadoEntrega.PENDIENTE));
        verify(repository, never()).save(any(Entrega.class));
    }

    @Test
    void repartidorInactivoLanzaConflicto() {
        EntregaRepository repository = mock(EntregaRepository.class);
        ClienteClient clienteClient = mock(ClienteClient.class);
        RepartidorClient repartidorClient = mock(RepartidorClient.class);
        EntregaServiceImpl service = new EntregaServiceImpl(repository, clienteClient, repartidorClient);
        when(clienteClient.obtener(1L)).thenReturn(new ClienteDto(1L, "Ana", "Perez", "ana@correo.com", "987654321", "Av. Lima 123"));
        when(repartidorClient.obtener(1L)).thenReturn(new RepartidorDto(1L, "Carlos", "Rojas", "999111222", "MOTO", false));

        EntregaRequest request = new EntregaRequest(1L, 1L, "Av. Lima 123", new BigDecimal("10.00"));

        assertThrows(ConflictoException.class, () -> service.crear(request));
        verify(repository, never()).save(any(Entrega.class));
    }

    private Entrega entregaConEstado(EstadoEntrega estado) {
        Entrega entrega = new Entrega();
        entrega.setId(1L);
        entrega.setIdCliente(1L);
        entrega.setIdRepartidor(1L);
        entrega.setDireccion("Av. Lima 123");
        entrega.setFecha(LocalDateTime.now());
        entrega.setCosto(new BigDecimal("10.00"));
        entrega.setEstado(estado);
        return entrega;
    }
}
