package org.acme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.dto.ReservaRequest;
import org.acme.model.Equipamento;
import org.acme.model.Reserva;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.model.StatusReserva;
import org.acme.repository.EquipamentoRepository;
import org.acme.repository.ReservaRepository;
import org.acme.repository.SalaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ReservaService {

    @Inject
    ReservaRepository reservaRepository;

    @Inject
    SalaRepository salaRepository;

    @Inject
    EquipamentoRepository equipamentoRepository;

    public List<Reserva> listarReservas() {
        return reservaRepository.listAll();
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> listarPorSala(Long salaId) {
        return reservaRepository.listarPorSala(salaId);
    }

    public boolean salaDisponivel(Long salaId, LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        Sala sala = buscarSalaObrigatoria(salaId);
        if (sala.getStatus() != StatusRecurso.DISPONIVEL) {
            return false;
        }
        return !reservaRepository.existeConflitoSala(salaId, inicio, fim, null);
    }

    public boolean equipamentoDisponivel(Long equipamentoId, LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        Equipamento equipamento = buscarEquipamentoObrigatorio(equipamentoId);
        if (equipamento.getStatus() != StatusRecurso.DISPONIVEL) {
            return false;
        }
        return !reservaRepository.existeConflitoEquipamento(equipamentoId, inicio, fim, null);
    }

    @Transactional
    public Reserva criarReserva(ReservaRequest request) {
        Reserva reserva = new Reserva();
        aplicarDados(reserva, request, null);
        reservaRepository.persist(reserva);
        return reserva;
    }

    @Transactional
    public Reserva atualizarReserva(Long id, ReservaRequest request) {
        Reserva reserva = reservaRepository.findById(id);

        if (reserva == null) {
            return null;
        }

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new BadRequestException("Reserva cancelada nao pode ser atualizada");
        }

        aplicarDados(reserva, request, id);
        return reserva;
    }

    @Transactional
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id);

        if (reserva == null) {
            return null;
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        return reserva;
    }

    @Transactional
    public boolean removerReserva(Long id) {
        return reservaRepository.deleteById(id);
    }

    private void aplicarDados(Reserva reserva, ReservaRequest request, Long reservaIgnoradaId) {
        validarRequest(request);

        Sala sala = buscarSalaObrigatoria(request.getSalaId());
        if (sala.getStatus() != StatusRecurso.DISPONIVEL) {
            throw new BadRequestException("Sala indisponivel no periodo informado");
        }

        Equipamento equipamento = null;
        if (request.getEquipamentoId() != null) {
            equipamento = buscarEquipamentoObrigatorio(request.getEquipamentoId());
            if (equipamento.getStatus() != StatusRecurso.DISPONIVEL) {
                throw new BadRequestException("Equipamento indisponivel no periodo informado");
            }
        }

        validarConflitos(request, reservaIgnoradaId);

        reserva.setSala(sala);
        reserva.setEquipamento(equipamento);
        reserva.setResponsavel(request.getResponsavel());
        reserva.setDataHoraInicio(request.getDataHoraInicio());
        reserva.setDataHoraFim(request.getDataHoraFim());

        if (reserva.getStatus() == null) {
            reserva.setStatus(StatusReserva.ATIVA);
        }
    }

    private void validarRequest(ReservaRequest request) {
        if (request == null) {
            throw new BadRequestException("Dados da reserva sao obrigatorios");
        }

        if (request.getSalaId() == null) {
            throw new BadRequestException("Sala e obrigatoria");
        }

        if (request.getResponsavel() == null || request.getResponsavel().isBlank()) {
            throw new BadRequestException("Responsavel e obrigatorio");
        }

        validarPeriodo(request.getDataHoraInicio(), request.getDataHoraFim());
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new BadRequestException("Data e hora de inicio e fim sao obrigatorias");
        }

        if (!inicio.isBefore(fim)) {
            throw new BadRequestException("Data e hora de inicio deve ser anterior ao fim");
        }
    }

    private void validarConflitos(ReservaRequest request, Long reservaIgnoradaId) {
        boolean salaOcupada = reservaRepository.existeConflitoSala(
                request.getSalaId(),
                request.getDataHoraInicio(),
                request.getDataHoraFim(),
                reservaIgnoradaId);

        if (salaOcupada) {
            throw new BadRequestException("Sala indisponivel no periodo informado");
        }

        if (request.getEquipamentoId() == null) {
            return;
        }

        boolean equipamentoOcupado = reservaRepository.existeConflitoEquipamento(
                request.getEquipamentoId(),
                request.getDataHoraInicio(),
                request.getDataHoraFim(),
                reservaIgnoradaId);

        if (equipamentoOcupado) {
            throw new BadRequestException("Equipamento indisponivel no periodo informado");
        }
    }

    private Sala buscarSalaObrigatoria(Long salaId) {
        Sala sala = salaRepository.findById(salaId);
        if (sala == null) {
            throw new NotFoundException("Sala nao encontrada");
        }
        return sala;
    }

    private Equipamento buscarEquipamentoObrigatorio(Long equipamentoId) {
        Equipamento equipamento = equipamentoRepository.findById(equipamentoId);
        if (equipamento == null) {
            throw new NotFoundException("Equipamento nao encontrado");
        }
        return equipamento;
    }
}
