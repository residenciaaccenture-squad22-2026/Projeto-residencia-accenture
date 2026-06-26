package org.acme.dto;

import org.acme.model.StatusRecurso;

import jakarta.validation.constraints.NotBlank;

public class PosicaoRequest {

    @NotBlank(message = "Codigo da posicao e obrigatorio")
    private String codigo;

    private String descricao;

    private String localizacao;

    private String recursos;

    private StatusRecurso status;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getRecursos() {
        return recursos;
    }

    public void setRecursos(String recursos) {
        this.recursos = recursos;
    }

    public StatusRecurso getStatus() {
        return status;
    }

    public void setStatus(StatusRecurso status) {
        this.status = status;
    }
}
