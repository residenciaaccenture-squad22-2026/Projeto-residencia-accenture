package org.acme.controller;

import java.net.URI;
import java.util.List;

import org.acme.model.Sala;
import org.acme.service.SalaService;

import jakarta.inject.Inject;
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
    public List<Sala> listarSalas() {
        return salaService.listarSalas();
    }

    @GET
    @Path("/{id}")
    public Sala buscarSala(@PathParam("id") Long id) {
        Sala sala = salaService.buscarPorId(id);

        if (sala == null) {
            throw new NotFoundException("Sala nao encontrada");
        }

        return sala;
    }

    @POST
    public Response cadastrarSala(Sala sala) {
        Sala salaCadastrada = salaService.cadastrarSala(sala);

        return Response
                .created(URI.create("/salas/" + salaCadastrada.getId()))
                .entity(salaCadastrada)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Sala atualizarSala(@PathParam("id") Long id, Sala sala) {
        Sala salaAtualizada = salaService.atualizarSala(id, sala);

        if (salaAtualizada == null) {
            throw new NotFoundException("Sala nao encontrada");
        }

        return salaAtualizada;
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
