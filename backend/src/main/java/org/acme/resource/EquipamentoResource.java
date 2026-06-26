package org.acme.resource;

import java.net.URI;
import java.util.List;

import org.acme.dto.ApiMapper;
import org.acme.dto.EquipamentoRequest;
import org.acme.dto.EquipamentoResponse;
import org.acme.model.Equipamento;
import org.acme.service.EquipamentoService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/equipamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EquipamentoResource {

    @Inject
    EquipamentoService equipamentoService;

    @GET
    public List<EquipamentoResponse> listarEquipamentos(@QueryParam("salaId") Long salaId,
            @QueryParam("posicaoId") Long posicaoId) {
        if (salaId != null) {
            return equipamentoService.listarPorSala(salaId).stream()
                    .map(EquipamentoResponse::from)
                    .toList();
        }

        if (posicaoId != null) {
            return equipamentoService.listarPorPosicao(posicaoId).stream()
                    .map(EquipamentoResponse::from)
                    .toList();
        }

        return equipamentoService.listarEquipamentos().stream()
                .map(EquipamentoResponse::from)
                .toList();
    }

    @POST
    public Response cadastrarEquipamento(@Valid EquipamentoRequest request) {
        Equipamento equipamentoCadastrado = equipamentoService.cadastrarEquipamento(
                ApiMapper.toEquipamento(request),
                request.getSalaId(),
                request.getPosicaoId());

        return Response
                .created(URI.create("/equipamentos/" + equipamentoCadastrado.getId()))
                .entity(EquipamentoResponse.from(equipamentoCadastrado))
                .build();
    }
}