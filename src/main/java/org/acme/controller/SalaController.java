package org.acme.controller;

import java.util.ArrayList;
import java.util.List;

import org.acme.model.Sala;

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

    private static final List<Sala> salas = new ArrayList<>();

    static {
        salas.add(new Sala(1L, "Sala Reunião 01", 10, "Bloco A", "Disponível"));
        salas.add(new Sala(2L, "Sala Treinamento", 25, "Bloco B", "Disponível"));
    }

    @GET
    public List<Sala> listarSalas() {
        return salas;
    }

    @POST
    public Sala cadastrarSala(Sala sala) {
        sala.setId((long) (salas.size() + 1));
        salas.add(sala);
        return sala;
    }
}