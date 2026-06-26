package org.acme.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.model.Reserva;
import org.acme.model.StatusReserva;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReservaRepository implements PanacheRepository<Reserva> {

    public List<Reserva> listarPorSala(Long salaId) {
        return list("sala.id", salaId);
    }

    public List<Reserva> listarPorPosicao(Long posicaoId) {
        return list("posicao.id", posicaoId);
    }

    public boolean existeConflitoSala(Long salaId, LocalDateTime inicio, LocalDateTime fim, Long reservaIgnoradaId) {
        Map<String, Object> params = new HashMap<>();
        params.put("salaId", salaId);
        params.put("inicio", inicio);
        params.put("fim", fim);
        params.put("status", StatusReserva.ATIVA.name());

        String filtroReservaIgnorada = "";
        if (reservaIgnoradaId != null) {
            params.put("reservaIgnoradaId", reservaIgnoradaId);
            filtroReservaIgnorada = "and id <> :reservaIgnoradaId";
        }

        return count("""
                sala.id = :salaId
                and status = :status
                and dataHoraInicio < :fim
                and dataHoraFim > :inicio
                %s
                """.formatted(filtroReservaIgnorada), params) > 0;
    }

    public boolean existeConflitoPosicao(Long posicaoId, LocalDateTime inicio, LocalDateTime fim,
            Long reservaIgnoradaId) {
        Map<String, Object> params = new HashMap<>();
        params.put("posicaoId", posicaoId);
        params.put("inicio", inicio);
        params.put("fim", fim);
        params.put("status", StatusReserva.ATIVA.name());

        String filtroReservaIgnorada = "";
        if (reservaIgnoradaId != null) {
            params.put("reservaIgnoradaId", reservaIgnoradaId);
            filtroReservaIgnorada = "and id <> :reservaIgnoradaId";
        }

        return count("""
                posicao.id = :posicaoId
                and status = :status
                and dataHoraInicio < :fim
                and dataHoraFim > :inicio
                %s
                """.formatted(filtroReservaIgnorada), params) > 0;
    }

}