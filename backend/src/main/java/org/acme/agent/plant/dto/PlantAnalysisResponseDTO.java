package org.acme.agent.plant.dto;

import java.util.ArrayList;
import java.util.List;

public class PlantAnalysisResponseDTO {

    private String sala;
    private int totalPos;
    private List<DetectedPositionDTO> pos = new ArrayList<>();
    private List<EquipmentSummaryDTO> resumoEq = new ArrayList<>();
    private String confGeral;
    private boolean revisao;
    private List<String> obs = new ArrayList<>();

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public int getTotalPos() {
        return totalPos;
    }

    public void setTotalPos(int totalPos) {
        this.totalPos = totalPos;
    }

    public List<DetectedPositionDTO> getPos() {
        return pos;
    }

    public void setPos(List<DetectedPositionDTO> pos) {
        this.pos = pos;
    }

    public List<EquipmentSummaryDTO> getResumoEq() {
        return resumoEq;
    }

    public void setResumoEq(List<EquipmentSummaryDTO> resumoEq) {
        this.resumoEq = resumoEq;
    }

    public String getConfGeral() {
        return confGeral;
    }

    public void setConfGeral(String confGeral) {
        this.confGeral = confGeral;
    }

    public boolean isRevisao() {
        return revisao;
    }

    public void setRevisao(boolean revisao) {
        this.revisao = revisao;
    }

    public List<String> getObs() {
        return obs;
    }

    public void setObs(List<String> obs) {
        this.obs = obs;
    }
}