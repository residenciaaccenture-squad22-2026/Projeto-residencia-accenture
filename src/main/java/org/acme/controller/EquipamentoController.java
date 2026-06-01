package org.acme.controller;

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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/equipamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EquipamentoController {

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

    @GET
    @Path("/{id}")
    public EquipamentoResponse buscarEquipamento(@PathParam("id") Long id) {
        Equipamento equipamento = equipamentoService.buscarPorId(id);

        if (equipamento == null) {
            throw new NotFoundException("Equipamento nao encontrado");
        }

        return EquipamentoResponse.from(equipamento);
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

    @PUT
    @Path("/{id}")
    public EquipamentoResponse atualizarEquipamento(@PathParam("id") Long id, @Valid EquipamentoRequest request) {
        Equipamento equipamentoAtualizado = equipamentoService.atualizarEquipamento(
                id,
                ApiMapper.toEquipamento(request),
                request.getSalaId(),
                request.getPosicaoId());

        if (equipamentoAtualizado == null) {
            throw new NotFoundException("Equipamento nao encontrado");
        }

        return EquipamentoResponse.from(equipamentoAtualizado);
    }

    @DELETE
    @Path("/{id}")
    public Response removerEquipamento(@PathParam("id") Long id) {
        boolean removido = equipamentoService.removerEquipamento(id);

        if (!removido) {
            throw new NotFoundException("Equipamento nao encontrado");
        }

        return Response.noContent().build();
    }
}
