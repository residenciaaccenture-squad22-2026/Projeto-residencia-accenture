package org.acme.dto;

import jakarta.validation.constraints.NotBlank;

public class PlantaImagemRequest {

    @NotBlank(message = "Imagem em base64 e obrigatoria")
    private String imagemBase64;

    private String mimeType = "image/png";

    private String nomeArquivo;

    private boolean cadastrar = true;

    public String getImagemBase64() {
        return imagemBase64;
    }

    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public boolean isCadastrar() {
        return cadastrar;
    }

    public void setCadastrar(boolean cadastrar) {
        this.cadastrar = cadastrar;
    }
}
