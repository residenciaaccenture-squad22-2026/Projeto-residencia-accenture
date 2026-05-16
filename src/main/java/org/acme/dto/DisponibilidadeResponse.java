package org.acme.dto;

public class DisponibilidadeResponse {

    private boolean disponivel;
    private String mensagem;

    public DisponibilidadeResponse() {
    }

    public DisponibilidadeResponse(boolean disponivel, String mensagem) {
        this.disponivel = disponivel;
        this.mensagem = mensagem;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
