package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.domain.*;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservaService {

    @Transactional
    public Reserva criarReserva(Long requisitanteId, Long alvoId, Long espacoId, LocalDateTime inicio, LocalDateTime fim) {
        Usuario requisitante = Usuario.findById(requisitanteId);
        Usuario alvo = Usuario.findById(alvoId);
        Espaco espaco = Espaco.findById(espacoId);

        if (!espaco.ativo) {
            throw new IllegalStateException("Esta sala/posição está desativada.");
        }

        // Regra 6: Admin pode reservar para outra pessoa
        if (!requisitante.equals(alvo) && requisitante.role != Role.ADMIN) {
            throw new SecurityException("Apenas administradores podem reservar para outras pessoas.");
        }

        // Regras 1, 3 e 5: Limites de reservas ativas
        long reservasAtivas = Reserva.count("usuario = ?1 and status = ?2", alvo, StatusReserva.ATIVA);
        if (alvo.role == Role.FUNCIONARIO && reservasAtivas >= 1) {
            throw new IllegalStateException("Funcionários só podem ter uma reserva ativa.");
        }

        Reserva reserva = new Reserva();
        reserva.usuario = alvo;
        reserva.espaco = espaco;
        reserva.dataInicio = inicio;
        reserva.dataFim = fim;
        reserva.status = StatusReserva.ATIVA;
        reserva.persist();

        return reserva;
    }

    @Transactional
    public void cancelarReserva(Long reservaId, Long acaoUsuarioId) {
        Reserva reserva = Reserva.findById(reservaId);
        Usuario executor = Usuario.findById(acaoUsuarioId);

        // Regras 2, 4 e 7: Permissões de cancelamento
        boolean isDono = reserva.usuario.equals(executor);
        boolean isAdmin = executor.role == Role.ADMIN;

        if (isDono || isAdmin) {
            reserva.status = StatusReserva.CANCELADA;
        } else {
            throw new SecurityException("Você não tem permissão para cancelar esta reserva.");
        }
    }

    @Transactional
    public void alterarStatusEspaco(Long espacoId, Long acaoUsuarioId, boolean ativo) {
        Usuario executor = Usuario.findById(acaoUsuarioId);

        // Regra 8: Apenas Admin desativa sala ou posição
        if (executor.role != Role.ADMIN) {
            throw new SecurityException("Apenas administradores podem ativar/desativar espaços.");
        }

        Espaco espaco = Espaco.findById(espacoId);
        espaco.ativo = ativo;
    }

    // Método de leitura para a IA usar
    public List<Espaco> buscarEspacosDisponiveis() {
        return Espaco.list("ativo", true);
    }
}