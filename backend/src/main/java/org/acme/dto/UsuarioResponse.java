package org.acme.dto;

import org.acme.model.Usuario;

public class UsuarioResponse {

    private Long id;
    private String nome;
    private String role;
    private String cargo;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Long id, String nome, String role, String cargo) {
        this.id = id;
        this.nome = nome;
        this.role = role;
        this.cargo = cargo;
    }

    public static UsuarioResponse from(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getRole(),
                usuario.getCargo());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
