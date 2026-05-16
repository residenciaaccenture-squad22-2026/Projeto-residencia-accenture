package org.acme.dto;

import org.acme.model.Equipamento;
import org.acme.model.StatusRecurso;

public class EquipamentoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private String tipo;
    private StatusRecurso status;

    public EquipamentoResponse() {
    }

    public EquipamentoResponse(Long id, String nome, String descricao, String tipo, StatusRecurso status) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.status = status;
    }

    public static EquipamentoResponse from(Equipamento equipamento) {
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getNome(),
                equipamento.getDescricao(),
                equipamento.getTipo(),
                equipamento.getStatus());
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public StatusRecurso getStatus() {
        return status;
    }

    public void setStatus(StatusRecurso status) {
        this.status = status;
    }
}
