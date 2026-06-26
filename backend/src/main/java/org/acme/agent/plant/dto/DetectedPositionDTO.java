package org.acme.agent.plant.dto;

import java.util.ArrayList;
import java.util.List;

public class DetectedPositionDTO {

    private String cod;
    private int lin;
    private int col;
    private ApproximateCoordinateDTO coord;
    private List<DetectedEquipmentDTO> eq = new ArrayList<>();
    private String conf;

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public int getLin() {
        return lin;
    }

    public void setLin(int lin) {
        this.lin = lin;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public ApproximateCoordinateDTO getCoord() {
        return coord;
    }

    public void setCoord(ApproximateCoordinateDTO coord) {
        this.coord = coord;
    }

    public List<DetectedEquipmentDTO> getEq() {
        return eq;
    }

    public void setEq(List<DetectedEquipmentDTO> eq) {
        this.eq = eq;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }
}