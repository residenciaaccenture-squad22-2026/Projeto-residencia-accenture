package org.acme.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StatusApiResponse {

    private String nome;
    private String status;
    private LocalDateTime dataHora;
    private List<String> endpoints;

    public StatusApiResponse() {
    }

    public StatusApiResponse(String nome, String status, List<String> endpoints) {
        this.nome = nome;
        this.status = status;
        this.endpoints = endpoints;
        this.dataHora = LocalDateTime.now();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}
