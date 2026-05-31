package org.acme.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Usuario extends PanacheEntity {
    public String nome;
    public enum role;  // "FUNCIONARIO", "GESTOR", "ADMIN"
    public String cargo; // "Designer", "Desenvolvedor Sênior", "Analista Financeiro"
}