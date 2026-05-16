package org.acme.controller;

import java.util.List;

import org.acme.dto.EquipamentoResponse;
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
public class DisponibilidadeController {

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
    @Path("/equipamentos")
    public List<EquipamentoResponse> listarEquipamentosDisponiveis(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        return disponibilidadeService.listarEquipamentosDisponiveis(
                DataHoraUtil.converter(inicio),
                DataHoraUtil.converter(fim)).stream()
                .map(EquipamentoResponse::from)
                .toList();
    }
}
