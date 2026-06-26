package org.acme.service;

import java.util.List;

import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.repository.SalaRepository;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SalaService {

    @Inject
    SalaRepository salaRepository;

    @CacheResult(cacheName = "salas")
    public List<Sala> listarSalas() {
        return salaRepository.listAll();
    }

    public Sala buscarPorId(Long id) {
        return salaRepository.findById(id);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "salas")
    public Sala cadastrarSala(Sala sala) {
        preencherStatusPadrao(sala);
        salaRepository.persist(sala);
        return sala;
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "salas")
    public Sala atualizarSala(Long id, Sala dadosAtualizados) {
        Sala sala = salaRepository.findById(id);

        if (sala == null) {
            return null;
        }

        sala.setNome(dadosAtualizados.getNome());
        sala.setCapacidade(dadosAtualizados.getCapacidade());
        sala.setLocalizacao(dadosAtualizados.getLocalizacao());
        sala.setStatus(dadosAtualizados.getStatus() != null ? dadosAtualizados.getStatus() : StatusRecurso.DISPONIVEL);

        return sala;
    }

    private void preencherStatusPadrao(Sala sala) {
        if (sala.getStatus() == null) {
            sala.setStatus(StatusRecurso.DISPONIVEL);
        }
    }
}