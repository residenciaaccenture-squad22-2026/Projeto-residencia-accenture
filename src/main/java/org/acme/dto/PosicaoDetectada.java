package org.acme.dto;

import java.util.ArrayList;
import java.util.List;

public class PosicaoDetectada {

    private String codigo;
    private String descricao;
    private String localizacao;
    private String recursos;
    private double confianca;
    private List<EquipamentoDetectado> equipamentos = new ArrayList<>();

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

    public double getConfianca() {
        return confianca;
    }

    public void setConfianca(double confianca) {
        this.confianca = confianca;
    }

    public List<EquipamentoDetectado> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<EquipamentoDetectado> equipamentos) {
        this.equipamentos = equipamentos;
    }
}
