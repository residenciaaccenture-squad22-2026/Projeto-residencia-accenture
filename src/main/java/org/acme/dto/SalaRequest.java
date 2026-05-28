package org.acme.dto;

import org.acme.model.StatusRecurso;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SalaRequest {

    @NotBlank(message = "Nome da sala e obrigatorio")
    private String nome;

    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    private int capacidade;

    private String localizacao;

    private StatusRecurso status;

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
}
