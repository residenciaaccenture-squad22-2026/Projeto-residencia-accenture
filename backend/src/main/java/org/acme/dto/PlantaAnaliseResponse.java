package org.acme.dto;

import java.util.ArrayList;
import java.util.List;

public class PlantaAnaliseResponse {

    private String resumo;
    private String observacoes;
    private SalaDetectada sala;
    private List<PosicaoDetectada> posicoes = new ArrayList<>();

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public SalaDetectada getSala() {
        return sala;
    }

    public void setSala(SalaDetectada sala) {
        this.sala = sala;
    }

    public List<PosicaoDetectada> getPosicoes() {
        return posicoes;
    }

    public void setPosicoes(List<PosicaoDetectada> posicoes) {
        this.posicoes = posicoes;
    }
}
