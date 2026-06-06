package org.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Usuario extends PanacheEntity {
    public String nome;

    @Enumerated(EnumType.STRING)
    public Role role;
}