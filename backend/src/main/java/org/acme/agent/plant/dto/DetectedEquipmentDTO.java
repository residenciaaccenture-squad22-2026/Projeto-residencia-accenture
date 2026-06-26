package org.acme.agent.plant.dto;

public class DetectedEquipmentDTO {

    private String t;
    private int q;
    private String c;

    public DetectedEquipmentDTO() {
    }

    public DetectedEquipmentDTO(String t, int q, String c) {
        this.t = t;
        this.q = q;
        this.c = c;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }
}