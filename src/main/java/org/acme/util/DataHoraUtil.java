package org.acme.util;

import java.time.LocalDateTime;

import jakarta.ws.rs.BadRequestException;

public final class DataHoraUtil {

    private DataHoraUtil() {
    }

    public static LocalDateTime converter(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Parametros inicio e fim sao obrigatorios");
        }

        try {
            return LocalDateTime.parse(valor);
        } catch (RuntimeException exception) {
            throw new BadRequestException("Use data e hora no formato ISO-8601, por exemplo 2026-05-15T14:00:00");
        }
    }
}
