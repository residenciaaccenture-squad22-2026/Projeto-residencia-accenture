package org.acme.controller;

import java.util.List;

import org.acme.model.Sala;
import org.acme.service.SalaService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/salas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalaController {

    private final SalaService salaService = new SalaService();

    @GET
    public List<Sala> listarSalas() {
        return salaService.listarSalas();
    }

    @POST
    public Sala cadastrarSala(Sala sala) {
        salaService.cadastrarSala(sala);
        return sala;
    }
}