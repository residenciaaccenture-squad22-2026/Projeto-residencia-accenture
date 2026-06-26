package org.acme.resource;

import java.util.List;

import org.acme.dto.PosicaoResponse;
import org.acme.dto.SalaResponse;
import org.acme.service.DisponibilidadeService;
import org.acme.util.DataHoraUtil;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/disponibilidade")
@Produces(MediaType.APPLICATION_JSON)
public class DisponibilidadeResource {

    @Inject
    DisponibilidadeService disponibilidadeService;

    @GET
    @Path("/salas")
    public List<SalaResponse> listarSalasDisponiveis(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        return disponibilidadeService.listarSalasDisponiveis(
                DataHoraUtil.converter(inicio),
                DataHoraUtil.converter(fim)).stream()
                .map(SalaResponse::from)
                .toList();
    }

    @GET
    @Path("/posicoes")
    public List<PosicaoResponse> listarPosicoesDisponiveis(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        return disponibilidadeService.listarPosicoesDisponiveis(
                DataHoraUtil.converter(inicio),
                DataHoraUtil.converter(fim)).stream()
                .map(PosicaoResponse::from)
                .toList();
    }

}

