package org.acme.controller;

import java.net.URI;
import java.util.List;

import org.acme.dto.ApiMapper;
import org.acme.dto.PosicaoRequest;
import org.acme.dto.PosicaoResponse;
import org.acme.model.Posicao;
import org.acme.service.PosicaoService;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/posicoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PosicaoController {

    @Inject
    PosicaoService posicaoService;

    @GET
    public List<PosicaoResponse> listarPosicoes() {
        return posicaoService.listarPosicoes().stream()
                .map(PosicaoResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public PosicaoResponse buscarPosicao(@PathParam("id") Long id) {
        Posicao posicao = posicaoService.buscarPorId(id);

        if (posicao == null) {
            throw new NotFoundException("Posicao nao encontrada");
        }

        return PosicaoResponse.from(posicao);
    }

    @POST
    public Response cadastrarPosicao(@Valid PosicaoRequest request) {
        Posicao posicaoCadastrada = posicaoService.cadastrarPosicao(ApiMapper.toPosicao(request));

        return Response
                .created(URI.create("/posicoes/" + posicaoCadastrada.getId()))
                .entity(PosicaoResponse.from(posicaoCadastrada))
                .build();
    }

    @PUT
    @Path("/{id}")
    public PosicaoResponse atualizarPosicao(@PathParam("id") Long id, @Valid PosicaoRequest request) {
        Posicao posicaoAtualizada = posicaoService.atualizarPosicao(id, ApiMapper.toPosicao(request));

        if (posicaoAtualizada == null) {
            throw new NotFoundException("Posicao nao encontrada");
        }

        return PosicaoResponse.from(posicaoAtualizada);
    }

    @DELETE
    @Path("/{id}")
    public Response removerPosicao(@PathParam("id") Long id) {
        boolean removida = posicaoService.removerPosicao(id);

        if (!removida) {
            throw new NotFoundException("Posicao nao encontrada");
        }

        return Response.noContent().build();
    }
}
