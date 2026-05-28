package org.acme.dto;

import org.acme.model.Equipamento;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static Sala toSala(SalaRequest request) {
        Sala sala = new Sala();
        sala.setNome(request.getNome());
        sala.setCapacidade(request.getCapacidade());
        sala.setLocalizacao(request.getLocalizacao());
        sala.setStatus(request.getStatus() != null ? request.getStatus() : StatusRecurso.DISPONIVEL);
        return sala;
    }

    public static Equipamento toEquipamento(EquipamentoRequest request) {
        Equipamento equipamento = new Equipamento();
        equipamento.setNome(request.getNome());
        equipamento.setDescricao(request.getDescricao());
        equipamento.setTipo(request.getTipo());
        equipamento.setStatus(request.getStatus() != null ? request.getStatus() : StatusRecurso.DISPONIVEL);
        return equipamento;
    }
}
