package org.acme.service;

import java.util.List;

import org.acme.model.Equipamento;
import org.acme.model.Posicao;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.repository.EquipamentoRepository;
import org.acme.repository.PosicaoRepository;
import org.acme.repository.SalaRepository;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class EquipamentoService {

    @Inject
    EquipamentoRepository equipamentoRepository;

    @Inject
    SalaRepository salaRepository;

    @Inject
    PosicaoRepository posicaoRepository;

    @CacheResult(cacheName = "equipamentos")
    public List<Equipamento> listarEquipamentos() {
        return equipamentoRepository.listAll();
    }

    public List<Equipamento> listarPorSala(Long salaId) {
        return equipamentoRepository.listarPorSala(salaId);
    }

    public List<Equipamento> listarPorPosicao(Long posicaoId) {
        return equipamentoRepository.listarPorPosicao(posicaoId);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "equipamentos")
    @CacheInvalidateAll(cacheName = "salas")
    @CacheInvalidateAll(cacheName = "posicoes")
    public Equipamento cadastrarEquipamento(Equipamento equipamento, Long salaId, Long posicaoId) {
        aplicarVinculo(equipamento, salaId, posicaoId);
        preencherStatusPadrao(equipamento);
        equipamentoRepository.persist(equipamento);
        return equipamento;
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

    private Posicao buscarPosicaoObrigatoria(Long posicaoId) {
        Posicao posicao = posicaoRepository.findById(posicaoId);
        if (posicao == null) {
            throw new NotFoundException("Posicao nao encontrada");
        }
        return posicao;
    }

    private void aplicarVinculo(Equipamento equipamento, Long salaId, Long posicaoId) {
        if (salaId == null && posicaoId == null) {
            throw new BadRequestException("Sala ou posicao do equipamento e obrigatoria");
        }

        if (salaId != null && posicaoId != null) {
            throw new BadRequestException("Informe apenas sala ou posicao por equipamento");
        }

        equipamento.setSala(salaId == null ? null : buscarSalaObrigatoria(salaId));
        equipamento.setPosicao(posicaoId == null ? null : buscarPosicaoObrigatoria(posicaoId));
    }
}