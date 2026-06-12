package org.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;

@Entity
public class Usuario extends PanacheEntity {
    public String nome;
    public String email;
    public Role role;

    // ESTRATÉGIA SENIOR: Callbacks do JPA
    // @PostLoad: Executa logo após os dados serem lidos do banco (Supabase)
    // @PrePersist: Executa antes de salvar um usuário novo
    @PostLoad
    @PrePersist
    public void aplicarRolePadrao() {
        if (this.role == null) {
            this.role = Role.FUNCIONARIO; // Se vier null do banco, o Quarkus entende como FUNCIONARIO
        }
    }
}