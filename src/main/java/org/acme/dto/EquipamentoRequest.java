package org.acme.dto;

import org.acme.model.StatusRecurso;

import jakarta.validation.constraints.NotBlank;

public class EquipamentoRequest {

    @NotBlank(message = "Nome do equipamento e obrigatorio")
    private String nome;

    private String descricao;

    @NotBlank(message = "Tipo do equipamento e obrigatorio")
    private String tipo;

    private StatusRecurso status;

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
