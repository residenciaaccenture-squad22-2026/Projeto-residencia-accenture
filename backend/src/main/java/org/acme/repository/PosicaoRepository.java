package org.acme.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.model.Posicao;
import org.acme.model.StatusRecurso;
import org.acme.model.StatusReserva;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PosicaoRepository implements PanacheRepository<Posicao> {

    public Posicao buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }

        return find("lower(codigo) = ?1", codigo.toLowerCase()).firstResult();
    }

    public List<Posicao> listarDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        return list("""
                status = ?4
                and id not in (
                    select reserva.posicao.id
                    from Reserva reserva
                    where reserva.status = ?1
                    and reserva.posicao is not null
                    and reserva.dataHoraInicio < ?3
                    and reserva.dataHoraFim > ?2
                )
                """, StatusReserva.ATIVA.name(), inicio, fim, StatusRecurso.DISPONIVEL);
    }
}
