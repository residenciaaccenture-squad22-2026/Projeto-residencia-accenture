package org.acme.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.model.StatusReserva;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SalaRepository implements PanacheRepository<Sala> {

    public Sala buscarPorNome(String nome) {
        if (nome == null) {
            return null;
        }

        return find("lower(nome) = ?1", nome.toLowerCase()).firstResult();
    }

    public List<Sala> listarDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        return list("""
                status = ?4
                and id not in (
                    select reserva.sala.id
                    from Reserva reserva
                    where reserva.status = ?1
                    and reserva.sala is not null
                    and reserva.dataHoraInicio < ?3
                    and reserva.dataHoraFim > ?2
                )
                """, StatusReserva.ATIVA.name(), inicio, fim, StatusRecurso.DISPONIVEL);
    }
}
