package org.acme.resource;

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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/posicoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PosicaoResource {

    @Inject
    PosicaoService posicaoService;

    @GET
    public List<PosicaoResponse> listarPosicoes() {
        return posicaoService.listarPosicoes().stream()
                .map(PosicaoResponse::from)
                .toList();
    }

    @POST
    public Response cadastrarPosicao(@Valid PosicaoRequest request) {
        Posicao posicaoCadastrada = posicaoService.cadastrarPosicao(ApiMapper.toPosicao(request));

        return Response
                .created(URI.create("/posicoes/" + posicaoCadastrada.getId()))
                .entity(PosicaoResponse.from(posicaoCadastrada))
                .build();
    }
}