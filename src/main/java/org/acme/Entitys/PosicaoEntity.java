package org.acme.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Posicao extends PanacheEntity {
    public String cogigo_cadeira;
    public String localizacao;
    public String status; // Ex: "DISPONIVEL", "RESERVADA", "INATIVA"
    public String recursos; // Ex: "Monitor 34 ultrawide, Mesa digitalizadora, Cadeira Ergonômica"
}