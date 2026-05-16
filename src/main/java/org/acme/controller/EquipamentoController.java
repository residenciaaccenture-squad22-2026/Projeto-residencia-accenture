package org.acme.controller;

import java.net.URI;
import java.util.List;

import org.acme.model.Equipamento;
import org.acme.service.EquipamentoService;

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

@Path("/equipamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EquipamentoController {

    @Inject
    EquipamentoService equipamentoService;

    @GET
    public List<Equipamento> listarEquipamentos() {
        return equipamentoService.listarEquipamentos();
    }

    @GET
    @Path("/{id}")
    public Equipamento buscarEquipamento(@PathParam("id") Long id) {
        Equipamento equipamento = equipamentoService.buscarPorId(id);

        if (equipamento == null) {
            throw new NotFoundException("Equipamento nao encontrado");
        }

        return equipamento;
    }

    @POST
    public Response cadastrarEquipamento(Equipamento equipamento) {
        Equipamento equipamentoCadastrado = equipamentoService.cadastrarEquipamento(equipamento);

        return Response
                .created(URI.create("/equipamentos/" + equipamentoCadastrado.getId()))
                .entity(equipamentoCadastrado)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Equipamento atualizarEquipamento(@PathParam("id") Long id, Equipamento equipamento) {
        Equipamento equipamentoAtualizado = equipamentoService.atualizarEquipamento(id, equipamento);

        if (equipamentoAtualizado == null) {
            throw new NotFoundException("Equipamento nao encontrado");
        }

        return equipamentoAtualizado;
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
