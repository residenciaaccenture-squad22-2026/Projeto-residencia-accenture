package org.acme.service;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.model.Sala;
import org.acme.repository.SalaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class DisponibilidadeService {

    @Inject
    SalaRepository salaRepository;

    public List<Sala> listarSalasDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        return salaRepository.listarDisponiveis(inicio, fim);
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new BadRequestException("Data e hora de inicio e fim sao obrigatorias");
        }

        if (!inicio.isBefore(fim)) {
            throw new BadRequestException("Data e hora de inicio deve ser anterior ao fim");
        }
    }
}
