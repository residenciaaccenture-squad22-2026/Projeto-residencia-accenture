package org.acme.dto;

import java.util.List;

import org.acme.model.Sala;
import org.acme.model.StatusRecurso;

public class SalaResponse {

    private Long id;
    private String nome;
    private int capacidade;
    private String localizacao;
    private StatusRecurso status;
    private List<EquipamentoResponse> equipamentos;

    public SalaResponse() {
    }

    public SalaResponse(Long id, String nome, int capacidade, String localizacao, StatusRecurso status,
            List<EquipamentoResponse> equipamentos) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.status = status;
        this.equipamentos = equipamentos;
    }

    public static SalaResponse from(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidade(),
                sala.getLocalizacao(),
                sala.getStatus(),
                sala.getEquipamentos().stream()
                        .map(EquipamentoResponse::from)
                        .toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public StatusRecurso getStatus() {
        return status;
    }

    public void setStatus(StatusRecurso status) {
        this.status = status;
    }

    public List<EquipamentoResponse> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<EquipamentoResponse> equipamentos) {
        this.equipamentos = equipamentos;
    }
}
