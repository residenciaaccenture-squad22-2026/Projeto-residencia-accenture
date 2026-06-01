package org.acme.repository;

import java.util.List;

import org.acme.model.Equipamento;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EquipamentoRepository implements PanacheRepository<Equipamento> {

    public List<Equipamento> listarPorSala(Long salaId) {
        return list("sala.id", salaId);
    }

    public List<Equipamento> listarPorPosicao(Long posicaoId) {
        return list("posicao.id", posicaoId);
    }

    public boolean existePorPosicaoNomeTipo(Long posicaoId, String nome, String tipo) {
        return count("posicao.id = ?1 and lower(nome) = ?2 and lower(tipo) = ?3",
                posicaoId,
                nome.toLowerCase(),
                tipo.toLowerCase()) > 0;
    }

    public boolean existePorSalaNomeTipo(Long salaId, String nome, String tipo) {
        return count("sala.id = ?1 and lower(nome) = ?2 and lower(tipo) = ?3",
                salaId,
                nome.toLowerCase(),
                tipo.toLowerCase()) > 0;
    }
}
