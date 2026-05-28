package org.acme.service;

import java.util.List;

import org.acme.model.Equipamento;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.repository.EquipamentoRepository;
import org.acme.repository.SalaRepository;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class EquipamentoService {

    @Inject
    EquipamentoRepository equipamentoRepository;

    @Inject
    SalaRepository salaRepository;

    @CacheResult(cacheName = "equipamentos")
    public List<Equipamento> listarEquipamentos() {
        return equipamentoRepository.listAll();
    }

    public List<Equipamento> listarPorSala(Long salaId) {
        return equipamentoRepository.listarPorSala(salaId);
    }

    public Equipamento buscarPorId(Long id) {
        return equipamentoRepository.findById(id);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    @CacheInvalidateAll(cacheName = "salas")
    public Equipamento cadastrarEquipamento(Equipamento equipamento, Long salaId) {
        equipamento.setSala(buscarSalaObrigatoria(salaId));
        preencherStatusPadrao(equipamento);
        equipamentoRepository.persist(equipamento);
        return equipamento;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    @CacheInvalidateAll(cacheName = "salas")
    public Equipamento atualizarEquipamento(Long id, Equipamento dadosAtualizados, Long salaId) {
        Equipamento equipamento = equipamentoRepository.findById(id);

        if (equipamento == null) {
            return null;
        }

        equipamento.setSala(buscarSalaObrigatoria(salaId));
        equipamento.setNome(dadosAtualizados.getNome());
        equipamento.setDescricao(dadosAtualizados.getDescricao());
        equipamento.setTipo(dadosAtualizados.getTipo());
        equipamento.setStatus(
                dadosAtualizados.getStatus() != null ? dadosAtualizados.getStatus() : StatusRecurso.DISPONIVEL);

        return equipamento;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    @CacheInvalidateAll(cacheName = "salas")
    public boolean removerEquipamento(Long id) {
        return equipamentoRepository.deleteById(id);
    }

    private void preencherStatusPadrao(Equipamento equipamento) {
        if (equipamento.getStatus() == null) {
            equipamento.setStatus(StatusRecurso.DISPONIVEL);
        }
    }

    private Sala buscarSalaObrigatoria(Long salaId) {
        Sala sala = salaRepository.findById(salaId);
        if (sala == null) {
            throw new NotFoundException("Sala nao encontrada");
        }
        return sala;
    }
}
