package org.acme.service;

import java.util.List;

import org.acme.model.Sala;
import org.acme.repository.SalaRepository;

public class SalaService {

    private final SalaRepository salaRepository = new SalaRepository();

    public List<Sala> listarSalas() {
        return salaRepository.listarTodas();
    }

    public void cadastrarSala(Sala sala) {
        salaRepository.salvar(sala);
    }
}