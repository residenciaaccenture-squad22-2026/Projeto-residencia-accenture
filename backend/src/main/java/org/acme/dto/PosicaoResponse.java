package org.acme.dto;

import java.util.List;

import org.acme.model.Posicao;
import org.acme.model.StatusRecurso;

public class PosicaoResponse {

    private Long id;
    private String codigo;
    private String descricao;
    private String localizacao;
    private String recursos;
    private StatusRecurso status;
    private List<EquipamentoResponse> equipamentos;

    public PosicaoResponse() {
    }

    public PosicaoResponse(Long id, String codigo, String descricao, String localizacao, String recursos,
            StatusRecurso status, List<EquipamentoResponse> equipamentos) {
        this.id = id;
        this.codigo = codigo;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.recursos = recursos;
        this.status = status;
        this.equipamentos = equipamentos;
    }

    public static PosicaoResponse from(Posicao posicao) {
        if (posicao == null) {
            return null;
        }

        return new PosicaoResponse(
                posicao.getId(),
                posicao.getCodigo(),
                posicao.getDescricao(),
                posicao.getLocalizacao(),
                posicao.getRecursos(),
                posicao.getStatus(),
                posicao.getEquipamentos().stream()
                        .map(EquipamentoResponse::from)
                        .toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getRecursos() {
        return recursos;
    }

    public void setRecursos(String recursos) {
        this.recursos = recursos;
    }

    public StatusRecurso getStatus() {
        return status;
    }

    public void setStatus(StatusRecurso status) {
        this.status = status;
    }

    public List<EquipamentoResponse> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<EquipamentoResponse> equipamentos) {
        this.equipamentos = equipamentos;
    }
}
