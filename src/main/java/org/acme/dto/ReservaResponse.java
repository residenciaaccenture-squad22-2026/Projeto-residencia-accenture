package org.acme.dto;

import java.time.LocalDateTime;

import org.acme.model.Reserva;
import org.acme.model.StatusReserva;

public class ReservaResponse {

    private Long id;
    private SalaResponse sala;
    private EquipamentoResponse equipamento;
    private String responsavel;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private StatusReserva status;

    public ReservaResponse() {
    }

    public ReservaResponse(Long id, SalaResponse sala, EquipamentoResponse equipamento, String responsavel,
            LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, StatusReserva status) {
        this.id = id;
        this.sala = sala;
        this.equipamento = equipamento;
        this.responsavel = responsavel;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.status = status;
    }

    public static ReservaResponse from(Reserva reserva) {
        EquipamentoResponse equipamento = reserva.getEquipamento() == null
                ? null
                : EquipamentoResponse.from(reserva.getEquipamento());

        return new ReservaResponse(
                reserva.getId(),
                SalaResponse.from(reserva.getSala()),
                equipamento,
                reserva.getResponsavel(),
                reserva.getDataHoraInicio(),
                reserva.getDataHoraFim(),
                reserva.getStatus());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SalaResponse getSala() {
        return sala;
    }

    public void setSala(SalaResponse sala) {
        this.sala = sala;
    }

    public EquipamentoResponse getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(EquipamentoResponse equipamento) {
        this.equipamento = equipamento;
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

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }
}
