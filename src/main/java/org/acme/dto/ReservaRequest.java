package org.acme.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReservaRequest {

    private Long salaId;

    private Long posicaoId;

    private Long usuarioId;

    @NotBlank(message = "Responsavel e obrigatorio")
    private String responsavel;

    @NotNull(message = "Data e hora de inicio e obrigatoria")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Data e hora de fim e obrigatoria")
    private LocalDateTime dataHoraFim;

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public Long getPosicaoId() {
        return posicaoId;
    }

    public void setPosicaoId(Long posicaoId) {
        this.posicaoId = posicaoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }
}
