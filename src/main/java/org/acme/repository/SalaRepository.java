package org.acme.repository;

import java.util.ArrayList;
import java.util.List;

import org.acme.model.Sala;

public class SalaRepository {

    private static final List<Sala> salas = new ArrayList<>();

    public List<Sala> listarTodas() {
        return salas;
    }

    public void salvar(Sala sala) {
        salas.add(sala);
    }
}