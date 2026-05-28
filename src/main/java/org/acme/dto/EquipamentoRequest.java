package org.acme.dto;

import org.acme.model.StatusRecurso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EquipamentoRequest {

    @NotNull(message = "Sala do equipamento e obrigatoria")
    private Long salaId;

    @NotBlank(message = "Nome do equipamento e obrigatorio")
    private String nome;

    private String descricao;

    @NotBlank(message = "Tipo do equipamento e obrigatorio")
    private String tipo;

    private StatusRecurso status;

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
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
