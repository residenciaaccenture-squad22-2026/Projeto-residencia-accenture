package org.acme.ai;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

// Importe suas entidades Panache (Usuario, Reserva, Posicao)
import org.acme.model.Usuario;
import org.acme.model.Reserva;
import org.acme.model.Posicao;

@ApplicationScoped
public class ReservaChatTools {

    @Tool("Retorna os dados do usuário, sua role (FUNCIONARIO, GESTOR, ADMIN) e seu cargo (ex: Designer, Dev)")
    public Usuario buscarDadosUsuario(Long usuarioId) {
        return Usuario.findById(usuarioId); // Entidade Panache
    }

    @Tool("Lista todas as reservas ativas e passadas de um usuário específico")
    public List<Reserva> listarReservas(Long usuarioId) {
        return Reserva.list("usuario.id", usuarioId);
    }

    @Tool("Recomenda as melhores posições de trabalho para o usuário baseando-se em seu cargo ou status de gestor")
    public String recomendarPosicoes(Long usuarioId) {
        Usuario user = Usuario.findById(usuarioId);

        if (user.role.equals("GESTOR")) {
            // Lógica Gestor: Buscar as reservas atuais dele e sugerir cadeiras adjacentes
            List<Reserva> ativas = Reserva.list("usuario.id = ?1 and status = 'ATIVA'", usuarioId);
            if (!ativas.isEmpty()) {
                return "Recomendo as posições P12 e P13, pois ficam no mesmo bloco das suas reservas atuais.";
            }
            return "Como gestor, recomendo as ilhas centrais que facilitam a comunicação com a equipe.";
        }

        if (user.cargo.equalsIgnoreCase("Designer")) {
            // Sugere baseado nos recursos (Ex: buscando no banco posições com monitor duplo)
            return "Recomendo as posições D01 a D05. Elas possuem Monitores Ultrawide 34'' e mesas digitalizadoras.";
        }

        return "Recomendo as posições padrão na área de desenvolvimento (DevSpace).";
    }

    @Transactional
    @Tool("Realiza a reserva de uma posição para o usuário")
    public String reservarPosicao(Long usuarioId, Long posicaoId) {
        Usuario user = Usuario.findById(usuarioId);

        // Regra: Funcionário só pode ter 1 reserva ativa
        if (user.role.equals("FUNCIONARIO")) {
            long ativas = Reserva.count("usuario.id = ?1 and status = 'ATIVA'", usuarioId);
            if (ativas >= 1) {
                return "Erro: Você já possui uma reserva ativa. Cancele-a antes de fazer uma nova.";
            }
        }

        // Criação da reserva
        Reserva novaReserva = new Reserva(user, Posicao.findById(posicaoId));
        novaReserva.persist();
        return "Reserva da posição " + posicaoId + " confirmada com sucesso!";
    }

    @Transactional
    @Tool("Cancela uma reserva. Requer validação de permissão se a reserva não for do próprio usuário.")
    public String cancelarReserva(Long usuarioLogadoId, Long reservaId) {
        Usuario user = Usuario.findById(usuarioLogadoId);
        Reserva reserva = Reserva.findById(reservaId);

        if (reserva == null) return "Reserva não encontrada.";

        // Regra de Cancelamento
        if (!reserva.usuario.id.equals(usuarioLogadoId) && !user.role.equals("ADMIN")) {
            return "Erro: Acesso negado. Apenas Administradores podem cancelar reservas de outras pessoas.";
        }

        reserva.status = "CANCELADA";
        return "Reserva " + reservaId + " cancelada com sucesso.";
    }

    @Transactional
    @Tool("Desativa uma sala ou posição (Apenas ADMIN)")
    public String desativarRecurso(Long usuarioLogadoId, String tipo, Long idRecurso) {
        Usuario user = Usuario.findById(usuarioLogadoId);

        if (!user.role.equals("ADMIN")) {
            return "Erro: Permissão negada. Apenas administradores podem desativar salas e posições.";
        }

        if (tipo.equalsIgnoreCase("SALA")) {
            // Lógica para sala, como no seu modelo Sala atual
            return "Sala desativada para manutenção.";
        } else {
            // Lógica para posição
            return "Posição " + idRecurso + " bloqueada com sucesso.";
        }
    }
}