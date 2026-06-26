package org.acme.dto;

import java.util.ArrayList;
import java.util.List;

public class PlantaImportacaoResponse {

    private PlantaAnaliseResponse analise;
    private int salasCadastradas;
    private int salasAtualizadas;
    private int posicoesCadastradas;
    private int posicoesAtualizadas;
    private int equipamentosCadastrados;
    private SalaResponse sala;
    private List<PosicaoResponse> posicoes = new ArrayList<>();

    public PlantaAnaliseResponse getAnalise() {
        return analise;
    }

    public void setAnalise(PlantaAnaliseResponse analise) {
        this.analise = analise;
    }

    public int getSalasCadastradas() {
        return salasCadastradas;
    }

    public void setSalasCadastradas(int salasCadastradas) {
        this.salasCadastradas = salasCadastradas;
    }

    public int getSalasAtualizadas() {
        return salasAtualizadas;
    }

    public void setSalasAtualizadas(int salasAtualizadas) {
        this.salasAtualizadas = salasAtualizadas;
    }

    public int getPosicoesCadastradas() {
        return posicoesCadastradas;
    }

    public void setPosicoesCadastradas(int posicoesCadastradas) {
        this.posicoesCadastradas = posicoesCadastradas;
    }

    public int getPosicoesAtualizadas() {
        return posicoesAtualizadas;
    }

    public void setPosicoesAtualizadas(int posicoesAtualizadas) {
        this.posicoesAtualizadas = posicoesAtualizadas;
    }

    public int getEquipamentosCadastrados() {
        return equipamentosCadastrados;
    }

    public void setEquipamentosCadastrados(int equipamentosCadastrados) {
        this.equipamentosCadastrados = equipamentosCadastrados;
    }

    public SalaResponse getSala() {
        return sala;
    }

    public void setSala(SalaResponse sala) {
        this.sala = sala;
    }

    public List<PosicaoResponse> getPosicoes() {
        return posicoes;
    }

    public void setPosicoes(List<PosicaoResponse> posicoes) {
        this.posicoes = posicoes;
    }
}
