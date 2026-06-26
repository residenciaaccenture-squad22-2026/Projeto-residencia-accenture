package org.acme.agent.plant.dto;

public class EquipmentSummaryDTO {

    private String t;
    private int qtd;

    public EquipmentSummaryDTO() {
    }

    public EquipmentSummaryDTO(String t, int qtd) {
        this.t = t;
        this.qtd = qtd;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }
}