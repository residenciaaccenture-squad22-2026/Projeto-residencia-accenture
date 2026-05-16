package org.acme.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.model.Equipamento;
import org.acme.model.StatusRecurso;
import org.acme.model.StatusReserva;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipamentoRepository implements PanacheRepository<Equipamento> {

    public List<Equipamento> listarDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        return list("""
                status = ?4
                and id not in (
                    select reserva.equipamento.id
                    from Reserva reserva
                    where reserva.equipamento is not null
                    and reserva.status = ?1
                    and reserva.dataHoraInicio < ?3
                    and reserva.dataHoraFim > ?2
                )
                """, StatusReserva.ATIVA, inicio, fim, StatusRecurso.DISPONIVEL);
    }
}
