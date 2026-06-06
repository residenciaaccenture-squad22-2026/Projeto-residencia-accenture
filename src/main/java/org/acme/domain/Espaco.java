package org.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Espaco extends PanacheEntity {
    public String nome;
    public boolean isPosicao; // false = Sala, true = Posição
    public boolean ativo = true; // Regra 8
    public int capacidade;
}