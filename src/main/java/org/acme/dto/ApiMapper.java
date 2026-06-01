package org.acme.dto;

import org.acme.model.Equipamento;
import org.acme.model.Posicao;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.model.Usuario;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static Sala toSala(SalaRequest request) {
        Sala sala = new Sala();
        sala.setNome(request.getNome());
        sala.setCapacidade(request.getCapacidade());
        sala.setLocalizacao(request.getLocalizacao());
        sala.setStatus(request.getStatus() != null ? request.getStatus() : StatusRecurso.DISPONIVEL);
        return sala;
    }

    public static Equipamento toEquipamento(EquipamentoRequest request) {
        Equipamento equipamento = new Equipamento();
        equipamento.setNome(request.getNome());
        equipamento.setDescricao(request.getDescricao());
        equipamento.setTipo(request.getTipo());
        equipamento.setStatus(request.getStatus() != null ? request.getStatus() : StatusRecurso.DISPONIVEL);
        return equipamento;
    }

    public static Posicao toPosicao(PosicaoRequest request) {
        Posicao posicao = new Posicao();
        posicao.setCodigo(request.getCodigo());
        posicao.setDescricao(request.getDescricao());
        posicao.setLocalizacao(request.getLocalizacao());
        posicao.setRecursos(request.getRecursos());
        posicao.setStatus(request.getStatus() != null ? request.getStatus() : StatusRecurso.DISPONIVEL);
        return posicao;
    }

    public static Usuario toUsuario(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setRole(request.getRole());
        usuario.setCargo(request.getCargo());
        return usuario;
    }
}
