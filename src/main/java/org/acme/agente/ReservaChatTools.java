package org.acme.agente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.acme.dto.ReservaRequest;
import org.acme.model.Posicao;
import org.acme.model.Reserva;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.model.StatusReserva;
import org.acme.model.Usuario;
import org.acme.repository.PosicaoRepository;
import org.acme.repository.ReservaRepository;
import org.acme.repository.UsuarioRepository;
import org.acme.service.DisponibilidadeService;
import org.acme.service.PosicaoService;
import org.acme.service.ReservaService;
import org.acme.service.SalaService;
import org.acme.util.DataHoraUtil;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReservaChatTools {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PosicaoRepository posicaoRepository;

    @Inject
    ReservaRepository reservaRepository;

    @Inject
    ReservaService reservaService;

    @Inject
    SalaService salaService;

    @Inject
    PosicaoService posicaoService;

    @Inject
    DisponibilidadeService disponibilidadeService;

    @Tool("Retorna os dados do usuario, incluindo role FUNCIONARIO, GESTOR ou ADMIN e cargo")
    public String buscarDadosUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null) {
            return "Usuario nao encontrado.";
        }

        return "Usuario{id=%d, nome='%s', role=%s, cargo='%s'}".formatted(
                usuario.getId(),
                usuario.getNome(),
                usuario.getRole(),
                usuario.getCargo());
    }

    @Tool("Lista todas as salas cadastradas com id, nome, capacidade, localizacao e status")
    public String listarSalas() {
        List<Sala> salas = salaService.listarSalas();
        if (salas.isEmpty()) {
            return "Nenhuma sala cadastrada.";
        }

        return salas.stream()
                .map(this::resumirSala)
                .toList()
                .toString();
    }

    @Tool("Lista todas as posicoes cadastradas com id, codigo, localizacao, recursos e status")
    public String listarPosicoes() {
        List<Posicao> posicoes = posicaoService.listarPosicoes();
        if (posicoes.isEmpty()) {
            return "Nenhuma posicao cadastrada.";
        }

        return posicoes.stream()
                .map(this::resumirPosicao)
                .toList()
                .toString();
    }

    @Tool("Lista as reservas ativas e canceladas de um usuario pelo id do usuario")
    public String listarReservas(Long usuarioId) {
        List<Reserva> reservas = reservaRepository.list("usuario.id", usuarioId);
        if (reservas.isEmpty()) {
            return "Nenhuma reserva encontrada para o usuario.";
        }

        return reservas.stream()
                .map(this::resumirReserva)
                .toList()
                .toString();
    }

    @Tool("Lista salas disponiveis em um periodo. Inicio e fim devem estar em ISO-8601")
    public String listarSalasDisponiveis(String inicio, String fim) {
        try {
            List<Sala> salas = disponibilidadeService.listarSalasDisponiveis(converterDataHora(inicio),
                    converterDataHora(fim));
            if (salas.isEmpty()) {
                return "Nao existem salas disponiveis no periodo informado.";
            }

            return salas.stream()
                    .map(this::resumirSala)
                    .toList()
                    .toString();
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    @Tool("Lista posicoes disponiveis em um periodo. Inicio e fim devem estar em ISO-8601")
    public String listarPosicoesDisponiveis(String inicio, String fim) {
        try {
            List<Posicao> posicoes = disponibilidadeService.listarPosicoesDisponiveis(converterDataHora(inicio),
                    converterDataHora(fim));
            if (posicoes.isEmpty()) {
                return "Nao existem posicoes disponiveis no periodo informado.";
            }

            return posicoes.stream()
                    .map(this::resumirPosicao)
                    .toList()
                    .toString();
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    @Tool("Recomenda posicoes de trabalho para o usuario considerando role, cargo e posicoes disponiveis")
    public String recomendarPosicoes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null) {
            return "Usuario nao encontrado.";
        }

        List<Posicao> disponiveis = posicaoRepository.list("status", StatusRecurso.DISPONIVEL);
        if (disponiveis.isEmpty()) {
            return "Nao ha posicoes disponiveis para recomendar agora.";
        }

        String cargo = usuario.getCargo() == null ? "" : usuario.getCargo().toLowerCase(Locale.ROOT);
        if (cargo.contains("designer")) {
            List<Posicao> recomendadas = disponiveis.stream()
                    .filter(posicao -> contem(posicao.getRecursos(), "monitor")
                            || contem(posicao.getRecursos(), "mesa")
                            || contem(posicao.getDescricao(), "design"))
                    .toList();

            if (!recomendadas.isEmpty()) {
                return "Para Designer, recomendo: " + recomendadas.stream()
                        .map(this::resumirPosicao)
                        .toList();
            }
        }

        if ("GESTOR".equalsIgnoreCase(usuario.getRole())) {
            long reservasAtivas = reservaRepository.count("usuario.id = ?1 and status = ?2",
                    usuarioId,
                    StatusReserva.ATIVA.name());
            String contexto = reservasAtivas > 0
                    ? "Como gestor com reservas ativas, priorize posicoes proximas da equipe: "
                    : "Como gestor, recomendo posicoes centrais: ";

            return contexto + disponiveis.stream()
                    .limit(5)
                    .map(this::resumirPosicao)
                    .toList();
        }

        return "Recomendo as primeiras posicoes disponiveis: " + disponiveis.stream()
                .limit(5)
                .map(this::resumirPosicao)
                .toList();
    }

    @Tool("Realiza reserva de uma posicao para o usuario. Inicio e fim devem estar em ISO-8601")
    public String reservarPosicao(Long usuarioId, Long posicaoId, String inicio, String fim) {
        try {
            Usuario usuario = buscarUsuarioObrigatorio(usuarioId);

            if ("FUNCIONARIO".equalsIgnoreCase(usuario.getRole()) && possuiReservaAtiva(usuarioId)) {
                return "Erro: voce ja possui uma reserva ativa. Cancele-a antes de fazer uma nova.";
            }

            ReservaRequest request = criarRequest(usuario, inicio, fim);
            request.setPosicaoId(posicaoId);

            Reserva reserva = reservaService.criarReserva(request);
            return "Reserva " + reserva.getId() + " da posicao "
                    + reserva.getPosicao().getCodigo() + " confirmada com sucesso.";
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    @Tool("Realiza reserva de uma sala para o usuario. Inicio e fim devem estar em ISO-8601")
    public String reservarSala(Long usuarioId, Long salaId, String inicio, String fim) {
        try {
            Usuario usuario = buscarUsuarioObrigatorio(usuarioId);

            ReservaRequest request = criarRequest(usuario, inicio, fim);
            request.setSalaId(salaId);

            Reserva reserva = reservaService.criarReserva(request);
            return "Reserva " + reserva.getId() + " da sala "
                    + reserva.getSala().getNome() + " confirmada com sucesso.";
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    @Tool("Cancela uma reserva. O usuario dono pode cancelar a propria reserva; ADMIN pode cancelar qualquer reserva")
    public String cancelarReserva(Long usuarioLogadoId, Long reservaId) {
        try {
            Usuario usuario = buscarUsuarioObrigatorio(usuarioLogadoId);
            Reserva reserva = reservaRepository.findById(reservaId);

            if (reserva == null) {
                return "Reserva nao encontrada.";
            }

            boolean donoDaReserva = reserva.getUsuario() != null
                    && reserva.getUsuario().getId().equals(usuarioLogadoId);
            boolean admin = "ADMIN".equalsIgnoreCase(usuario.getRole());

            if (!donoDaReserva && !admin) {
                return "Erro: acesso negado. Apenas administradores podem cancelar reservas de outras pessoas.";
            }

            reservaService.cancelarReserva(reservaId);
            return "Reserva " + reservaId + " cancelada com sucesso.";
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    @Tool("Desativa uma sala ou posicao. Apenas usuarios ADMIN podem executar")
    public String desativarRecurso(Long usuarioLogadoId, String tipo, Long idRecurso) {
        try {
            Usuario usuario = buscarUsuarioObrigatorio(usuarioLogadoId);
            if (!"ADMIN".equalsIgnoreCase(usuario.getRole())) {
                return "Erro: permissao negada. Apenas administradores podem desativar salas e posicoes.";
            }

            if ("SALA".equalsIgnoreCase(tipo)) {
                Sala sala = salaService.buscarPorId(idRecurso);
                if (sala == null) {
                    return "Sala nao encontrada.";
                }

                Sala dadosAtualizados = new Sala();
                dadosAtualizados.setNome(sala.getNome());
                dadosAtualizados.setCapacidade(sala.getCapacidade());
                dadosAtualizados.setLocalizacao(sala.getLocalizacao());
                dadosAtualizados.setStatus(StatusRecurso.MANUTENCAO);
                salaService.atualizarSala(idRecurso, dadosAtualizados);
                return "Sala " + sala.getNome() + " colocada em manutencao.";
            }

            if ("POSICAO".equalsIgnoreCase(tipo) || "POSI\u00c7\u00c3O".equalsIgnoreCase(tipo)) {
                Posicao posicao = posicaoService.buscarPorId(idRecurso);
                if (posicao == null) {
                    return "Posicao nao encontrada.";
                }

                Posicao dadosAtualizados = new Posicao();
                dadosAtualizados.setCodigo(posicao.getCodigo());
                dadosAtualizados.setDescricao(posicao.getDescricao());
                dadosAtualizados.setLocalizacao(posicao.getLocalizacao());
                dadosAtualizados.setRecursos(posicao.getRecursos());
                dadosAtualizados.setStatus(StatusRecurso.MANUTENCAO);
                posicaoService.atualizarPosicao(idRecurso, dadosAtualizados);
                return "Posicao " + posicao.getCodigo() + " colocada em manutencao.";
            }

            return "Tipo de recurso invalido. Use SALA ou POSICAO.";
        } catch (RuntimeException exception) {
            return mensagemErro(exception);
        }
    }

    private ReservaRequest criarRequest(Usuario usuario, String inicio, String fim) {
        ReservaRequest request = new ReservaRequest();
        request.setUsuarioId(usuario.getId());
        request.setResponsavel(usuario.getNome());
        request.setDataHoraInicio(converterDataHora(inicio));
        request.setDataHoraFim(converterDataHora(fim));
        return request;
    }

    private Usuario buscarUsuarioObrigatorio(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null) {
            throw new WebApplicationException("Usuario nao encontrado.", 404);
        }
        return usuario;
    }

    private boolean possuiReservaAtiva(Long usuarioId) {
        return reservaRepository.count("usuario.id = ?1 and status = ?2", usuarioId, StatusReserva.ATIVA.name()) > 0;
    }

    private LocalDateTime converterDataHora(String valor) {
        return DataHoraUtil.converter(valor);
    }

    private String resumirSala(Sala sala) {
        return "Sala{id=%d, nome='%s', capacidade=%d, localizacao='%s', status=%s}".formatted(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidade(),
                sala.getLocalizacao(),
                sala.getStatus());
    }

    private String resumirPosicao(Posicao posicao) {
        return "Posicao{id=%d, codigo='%s', localizacao='%s', recursos='%s', status=%s}".formatted(
                posicao.getId(),
                posicao.getCodigo(),
                posicao.getLocalizacao(),
                posicao.getRecursos(),
                posicao.getStatus());
    }

    private String resumirReserva(Reserva reserva) {
        String recurso = reserva.getSala() != null
                ? "sala='" + reserva.getSala().getNome() + "'"
                : "posicao='" + reserva.getPosicao().getCodigo() + "'";

        return "Reserva{id=%d, %s, responsavel='%s', inicio=%s, fim=%s, status=%s}".formatted(
                reserva.getId(),
                recurso,
                reserva.getResponsavel(),
                reserva.getDataHoraInicio(),
                reserva.getDataHoraFim(),
                reserva.getStatus());
    }

    private boolean contem(String valor, String trecho) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(trecho);
    }

    private String mensagemErro(RuntimeException exception) {
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getMessage() != null
                && !webApplicationException.getMessage().isBlank()) {
            return "Erro: " + webApplicationException.getMessage();
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return "Erro: " + exception.getMessage();
        }

        return "Erro ao executar a operacao solicitada.";
    }
}
