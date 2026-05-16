package org.acme.controller;

import java.net.URI;
import java.util.List;

import org.acme.dto.ApiMapper;
import org.acme.dto.SalaRequest;
import org.acme.dto.SalaResponse;
import org.acme.model.Sala;
import org.acme.service.SalaService;

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

@Path("/salas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalaController {

    @Inject
    SalaService salaService;

    @GET
    public List<SalaResponse> listarSalas() {
        return salaService.listarSalas().stream()
                .map(SalaResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public SalaResponse buscarSala(@PathParam("id") Long id) {
        Sala sala = salaService.buscarPorId(id);

        if (sala == null) {
            throw new NotFoundException("Sala nao encontrada");
        }

        return SalaResponse.from(sala);
    }

    @POST
    public Response cadastrarSala(@Valid SalaRequest request) {
        Sala salaCadastrada = salaService.cadastrarSala(ApiMapper.toSala(request));

        return Response
                .created(URI.create("/salas/" + salaCadastrada.getId()))
                .entity(SalaResponse.from(salaCadastrada))
                .build();
    }

    @PUT
    @Path("/{id}")
    public SalaResponse atualizarSala(@PathParam("id") Long id, @Valid SalaRequest request) {
        Sala salaAtualizada = salaService.atualizarSala(id, ApiMapper.toSala(request));

        if (salaAtualizada == null) {
            throw new NotFoundException("Sala nao encontrada");
        }

        return SalaResponse.from(salaAtualizada);
    }

    @DELETE
    @Path("/{id}")
    public Response removerSala(@PathParam("id") Long id) {
        boolean removida = salaService.removerSala(id);

        if (!removida) {
            throw new NotFoundException("Sala nao encontrada");
        }

        return Response.noContent().build();
    }
}
