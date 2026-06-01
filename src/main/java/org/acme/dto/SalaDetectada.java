package org.acme.dto;

import java.util.ArrayList;
import java.util.List;

public class SalaDetectada {

    private String nome;
    private String descricao;
    private String localizacao;
    private int capacidade;
    private double confianca;
    private List<EquipamentoDetectado> equipamentos = new ArrayList<>();

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

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
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
