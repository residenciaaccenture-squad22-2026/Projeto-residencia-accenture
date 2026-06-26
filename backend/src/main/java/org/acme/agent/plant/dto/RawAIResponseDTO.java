package org.acme.agent.plant.dto;

public class RawAIResponseDTO {

    private String content;

    public RawAIResponseDTO() {
    }

    public RawAIResponseDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}