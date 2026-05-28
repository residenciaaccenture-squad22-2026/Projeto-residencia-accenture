package org.acme.repository;

import java.util.List;

import org.acme.model.Equipamento;
import org.acme.model.StatusRecurso;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipamentoRepository implements PanacheRepository<Equipamento> {

    public List<Equipamento> listarPorSala(Long salaId) {
        return list("sala.id", salaId);
    }

    public List<Equipamento> listarDisponiveis() {
        return list("status", StatusRecurso.DISPONIVEL);
    }
}
