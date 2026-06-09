package org.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Reserva extends PanacheEntity {
    @ManyToOne
    public Usuario usuario;

    @ManyToOne
    public Espaco espaco;

    public LocalDateTime dataInicio;
    public LocalDateTime dataFim;

    public String status;
}