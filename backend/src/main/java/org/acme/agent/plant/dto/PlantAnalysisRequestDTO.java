package org.acme.agent.plant.dto;

public class PlantAnalysisRequestDTO {

    private String imageUrl;
    private String base64Image;
    private String observacao;
    private String roomNameHint;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getRoomNameHint() {
        return roomNameHint;
    }

    public void setRoomNameHint(String roomNameHint) {
        this.roomNameHint = roomNameHint;
    }
}