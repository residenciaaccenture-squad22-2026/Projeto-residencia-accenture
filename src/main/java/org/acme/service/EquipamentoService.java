package org.acme.service;

import java.util.List;

import org.acme.model.Equipamento;
import org.acme.model.StatusRecurso;
import org.acme.repository.EquipamentoRepository;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EquipamentoService {

    @Inject
    EquipamentoRepository equipamentoRepository;

    @CacheResult(cacheName = "equipamentos")
    public List<Equipamento> listarEquipamentos() {
        return equipamentoRepository.listAll();
    }

    public Equipamento buscarPorId(Long id) {
        return equipamentoRepository.findById(id);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    public Equipamento cadastrarEquipamento(Equipamento equipamento) {
        preencherStatusPadrao(equipamento);
        equipamentoRepository.persist(equipamento);
        return equipamento;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    public Equipamento atualizarEquipamento(Long id, Equipamento dadosAtualizados) {
        Equipamento equipamento = equipamentoRepository.findById(id);

        if (equipamento == null) {
            return null;
        }

        equipamento.setNome(dadosAtualizados.getNome());
        equipamento.setDescricao(dadosAtualizados.getDescricao());
        equipamento.setTipo(dadosAtualizados.getTipo());
        equipamento.setStatus(
                dadosAtualizados.getStatus() != null ? dadosAtualizados.getStatus() : StatusRecurso.DISPONIVEL);

        return equipamento;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    public boolean removerEquipamento(Long id) {
        return equipamentoRepository.deleteById(id);
    }

    private void preencherStatusPadrao(Equipamento equipamento) {
        if (equipamento.getStatus() == null) {
            equipamento.setStatus(StatusRecurso.DISPONIVEL);
        }
    }
}
