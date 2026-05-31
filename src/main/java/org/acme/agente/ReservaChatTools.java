package org.acme.ai;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

import org.acme.model.Usuario;
import org.acme.model.Reserva;
import org.acme.model.Posicao;

@ApplicationScoped
public class ReservaChatTools {

    @Tool("Busca os dados do usuário atual, incluindo sua role e seu cargo exato na empresa")
    public Usuario buscarDadosUsuario(Long usuarioId) {
        return Usuario.findById(usuarioId);
    }

    @Tool("Lista todas as reservas ativas do usuário")
    public List<Reserva> listarReservasAtivas(Long usuarioId) {
        // Retorna apenas reservas com status ATIVA no banco
        return Reserva.list("usuario.id = ?1 and status = 'ATIVA'", usuarioId);
    }

    @Tool("Busca no banco de dados todas as posições de trabalho que estão atualmente com status DISPONIVEL")
    public List<Posicao> buscarPosicoesDisponiveis() {
        // Aqui pegamos dinamicamente do banco todas as mesas livres para a IA analisar
        return Posicao.list("status", "DISPONIVEL");
    }

    @Transactional
    @Tool("Efetua a reserva de uma posição específica para o usuário")
    public String reservarPosicao(Long usuarioId, Long posicaoId) {
        Usuario user = Usuario.findById(usuarioId);
        Posicao posicao = Posicao.findById(posicaoId);

        if (posicao == null || !posicao.status.equals("DISPONIVEL")) {
            return "Erro: Posição não existe ou já está ocupada.";
        }

        // Regra de negócio estrita mantida no Java por segurança
        if (user.role.equals("FUNCIONARIO")) {
            long ativas = Reserva.count("usuario.id = ?1 and status = 'ATIVA'", usuarioId);
            if (ativas >= 1) {
                return "Erro de regra de negócio: Funcionários normais só podem ter 1 reserva ativa por vez. Peça para o usuário cancelar a anterior.";
            }
        }

        // Efetua a reserva
        Reserva novaReserva = new Reserva();
        novaReserva.usuario = user;
        novaReserva.posicao = posicao;
        novaReserva.status = "ATIVA";
        novaReserva.persist();

        // Atualiza status da posição
        posicao.disponivel = "FALSE";
        posicao.persist();

        return "Reserva realizada com sucesso para a posição " + posicao.nome;
    }

    @Transactional
    @Tool("Cancela uma reserva pelo seu ID")
    public String cancelarReserva(Long usuarioId, Long reservaId) {
        Usuario user = Usuario.findById(usuarioId);
        Reserva reserva = Reserva.findById(reservaId);

        if (reserva == null) return "Reserva não encontrada.";

        if (!reserva.usuario.id.equals(usuarioId) && !user.role.equals("ADMIN")) {
            return "Erro: Acesso negado. Apenas ADMIN pode cancelar reserva de terceiros.";
        }

        reserva.status = "CANCELADA";
        // Libera a posição
        reserva.posicao.status = "DISPONIVEL";

        return "Reserva " + reservaId + " cancelada com sucesso.";
    }

    @Transactional
    @Tool("Desativa uma sala ou posição mudando o status para INATIVA (Exige ser ADMIN)")
    public String desativarPosicao(Long usuarioId, Long posicaoId) {
        Usuario user = Usuario.findById(usuarioId);
        if (!user.role.equals("ADMIN")) {
            return "Erro: Permissão negada. Apenas ADMIN.";
        }
        Posicao pos = Posicao.findById(posicaoId);
        pos.status = "INATIVA";
        return "Posição inativada com sucesso.";
    }
}