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

    public boolean existeConflitoSala(Long salaId, LocalDateTime inicio, LocalDateTime fim, Long reservaIgnoradaId) {
        Map<String, Object> params = new HashMap<>();
        params.put("salaId", salaId);
        params.put("inicio", inicio);
        params.put("fim", fim);
        params.put("status", StatusReserva.ATIVA);

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

    public boolean existeConflitoEquipamento(Long equipamentoId, LocalDateTime inicio, LocalDateTime fim,
            Long reservaIgnoradaId) {
        Map<String, Object> params = new HashMap<>();
        params.put("equipamentoId", equipamentoId);
        params.put("inicio", inicio);
        params.put("fim", fim);
        params.put("status", StatusReserva.ATIVA);

        String filtroReservaIgnorada = "";
        if (reservaIgnoradaId != null) {
            params.put("reservaIgnoradaId", reservaIgnoradaId);
            filtroReservaIgnorada = "and id <> :reservaIgnoradaId";
        }

        return count("""
                equipamento.id = :equipamentoId
                and status = :status
                and dataHoraInicio < :fim
                and dataHoraFim > :inicio
                %s
                """.formatted(filtroReservaIgnorada), params) > 0;
    }
}
