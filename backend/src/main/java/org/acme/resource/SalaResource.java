package org.acme.resource;

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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/salas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalaResource {

    @Inject
    SalaService salaService;

    @GET
    public List<SalaResponse> listarSalas() {
        return salaService.listarSalas().stream()
                .map(SalaResponse::from)
                .toList();
    }

    @POST
    public Response cadastrarSala(@Valid SalaRequest request) {
        Sala salaCadastrada = salaService.cadastrarSala(ApiMapper.toSala(request));

        return Response
                .created(URI.create("/salas/" + salaCadastrada.getId()))
                .entity(SalaResponse.from(salaCadastrada))
                .build();
    }
}