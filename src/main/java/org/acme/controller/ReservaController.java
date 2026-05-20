package org.acme.controller;

import java.net.URI;
import java.util.List;

import org.acme.dto.DisponibilidadeResponse;
import org.acme.dto.ReservaRequest;
import org.acme.dto.ReservaResponse;
import org.acme.model.Reserva;
import org.acme.service.ReservaService;
import org.acme.util.DataHoraUtil;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/reservas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservaController {

    @Inject
    ReservaService reservaService;

    @GET
    public List<ReservaResponse> listarReservas(@QueryParam("salaId") Long salaId) {
        if (salaId != null) {
            return reservaService.listarPorSala(salaId).stream()
                    .map(ReservaResponse::from)
                    .toList();
        }

        return reservaService.listarReservas().stream()
                .map(ReservaResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public ReservaResponse buscarReserva(@PathParam("id") Long id) {
        Reserva reserva = reservaService.buscarPorId(id);

        if (reserva == null) {
            throw new NotFoundException("Reserva nao encontrada");
        }

        return ReservaResponse.from(reserva);
    }

    @GET
    @Path("/disponibilidade/sala/{salaId}")
    public DisponibilidadeResponse consultarDisponibilidadeSala(
            @PathParam("salaId") Long salaId,
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim) {
        boolean disponivel = reservaService.salaDisponivel(salaId, DataHoraUtil.converter(inicio),
                DataHoraUtil.converter(fim));
        String mensagem = disponivel ? "Sala disponivel" : "Sala indisponivel no periodo informado";

        return new DisponibilidadeResponse(disponivel, mensagem);
    }

    @POST
    public Response criarReserva(@Valid ReservaRequest request) {
        Reserva reserva = reservaService.criarReserva(request);

        return Response
                .created(URI.create("/reservas/" + reserva.getId()))
                .entity(ReservaResponse.from(reserva))
                .build();
    }

    @PUT
    @Path("/{id}")
    public ReservaResponse atualizarReserva(@PathParam("id") Long id, @Valid ReservaRequest request) {
        Reserva reserva = reservaService.atualizarReserva(id, request);

        if (reserva == null) {
            throw new NotFoundException("Reserva nao encontrada");
        }

        return ReservaResponse.from(reserva);
    }

    @PUT
    @Path("/{id}/cancelar")
    public ReservaResponse cancelarReserva(@PathParam("id") Long id) {
        Reserva reserva = reservaService.cancelarReserva(id);

        if (reserva == null) {
            throw new NotFoundException("Reserva nao encontrada");
        }

        return ReservaResponse.from(reserva);
    }

    @DELETE
    @Path("/{id}")
    public Response removerReserva(@PathParam("id") Long id) {
        boolean removida = reservaService.removerReserva(id);

        if (!removida) {
            throw new NotFoundException("Reserva nao encontrada");
        }

        return Response.noContent().build();
    }
}
