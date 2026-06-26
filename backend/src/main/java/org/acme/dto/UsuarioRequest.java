package org.acme.dto;

import jakarta.validation.constraints.NotBlank;

public class UsuarioRequest {

    @NotBlank(message = "Nome do usuario e obrigatorio")
    private String nome;

    @NotBlank(message = "Role do usuario e obrigatoria")
    private String role;

    private String cargo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
