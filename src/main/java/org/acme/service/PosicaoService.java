package org.acme.service;

import java.util.List;

import org.acme.model.Posicao;
import org.acme.model.StatusRecurso;
import org.acme.repository.PosicaoRepository;
import org.acme.repository.ReservaRepository;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PosicaoService {

    @Inject
    PosicaoRepository posicaoRepository;

    @Inject
    ReservaRepository reservaRepository;

    @CacheResult(cacheName = "posicoes")
    public List<Posicao> listarPosicoes() {
        return posicaoRepository.listAll();
    }

    public Posicao buscarPorId(Long id) {
        return posicaoRepository.findById(id);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "posicoes")
    public Posicao cadastrarPosicao(Posicao posicao) {
        preencherStatusPadrao(posicao);
        posicaoRepository.persist(posicao);
        return posicao;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "posicoes")
    public Posicao atualizarPosicao(Long id, Posicao dadosAtualizados) {
        Posicao posicao = posicaoRepository.findById(id);

        if (posicao == null) {
            return null;
        }

        posicao.setCodigo(dadosAtualizados.getCodigo());
        posicao.setDescricao(dadosAtualizados.getDescricao());
        posicao.setLocalizacao(dadosAtualizados.getLocalizacao());
        posicao.setRecursos(dadosAtualizados.getRecursos());
        posicao.setStatus(dadosAtualizados.getStatus() != null ? dadosAtualizados.getStatus() : StatusRecurso.DISPONIVEL);

        return posicao;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "posicoes")
    public boolean removerPosicao(Long id) {
        if (reservaRepository.existeReservaParaPosicao(id)) {
            throw new WebApplicationException("Posicao possui reservas vinculadas", Response.Status.CONFLICT);
        }

        return posicaoRepository.deleteById(id);
    }

    private void preencherStatusPadrao(Posicao posicao) {
        if (posicao.getStatus() == null) {
            posicao.setStatus(StatusRecurso.DISPONIVEL);
        }
    }
}
