package org.acme.agente;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import dev.langchain4j.agent.tool.Tool;
import org.acme.domain.Usuario;
import org.acme.domain.Reserva;
import org.acme.domain.Espaco;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservaChatTools {

    @Tool("Buscar todos os usuários cadastrados")
    public List<Usuario> buscarUsuarios() {
        return Usuario.listAll();
    }

    @Tool("Buscar todas as reservas cadastradas")
    public List<Reserva> buscarReservas() {
        return Reserva.listAll();
    }

    @Tool("Buscar todos os espaços (salas/posições) cadastrados no sistema")
    public List<Espaco> buscarEspacos() {
        return Espaco.listAll();
    }

    @Tool("Buscar espaços que estão ativos e disponíveis para uso")
    public List<Espaco> buscarEspacosDisponiveis() {
        // CORREÇÃO: Na classe Espaco o atributo de disponibilidade se chama 'ativo' (boolean)
        return Espaco.list("ativo", true);
    }

    @Tool("Criar uma nova reserva de espaço")
    @Transactional
    public String criarReserva(Long usuarioId, Long espacoId, String inicioStr, String fimStr) {
        Usuario usuario = Usuario.findById(usuarioId);
        Espaco espaco = Espaco.findById(espacoId);

        if (usuario == null) return "Erro: Usuário não encontrado.";
        if (espaco == null) return "Erro: Espaço não encontrado.";
        if (!espaco.ativo) return "Erro: Este espaço não está ativo para reservas.";

        try {
            LocalDateTime inicio = LocalDateTime.parse(inicioStr);
            LocalDateTime fim = LocalDateTime.parse(fimStr);

            Reserva reserva = new Reserva();
            reserva.usuario = usuario;
            reserva.espaco = espaco;
            reserva.dataInicio = inicio;
            reserva.dataFim = fim;
            reserva.status = "ATIVA"; // Usando a String conforme configurado anteriormente

            reserva.persist();
            return "Reserva criada com sucesso! ID: " + reserva.id;
        } catch (Exception e) {
            return "Erro ao criar reserva: " + e.getMessage();
        }
    }

    @Tool("Cancelar uma reserva existente")
    @Transactional
    public String cancelarReserva(Long reservaId) {
        Reserva reserva = Reserva.findById(reservaId);
        if (reserva == null) return "Erro: Reserva não encontrada.";

        // CORREÇÃO: O atributo na classe Reserva chama-se 'status'
        reserva.status = "CANCELADA";
        return "Reserva " + reservaId + " cancelada com sucesso.";
    }

    @Tool("Bloquear ou desativar um espaço temporariamente")
    @Transactional
    public String alterarStatusEspaco(Long espacoId, boolean ativo) {
        Espaco espaco = Espaco.findById(espacoId);
        if (espaco == null) return "Erro: Espaço não encontrado.";

        // CORREÇÃO: O atributo na classe Espaco chama-se 'ativo'
        espaco.ativo = ativo;
        String statusTexto = ativo ? "ativado" : "desativado";
        return "O espaço " + espaco.nome + " foi " + statusTexto + " com sucesso.";
    }
}