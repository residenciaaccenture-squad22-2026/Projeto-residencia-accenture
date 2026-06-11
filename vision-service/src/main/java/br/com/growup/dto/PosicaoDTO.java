package br.com.growup.dto.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PosicaoDTO(
    Long id,
    @JsonProperty("mesa_id") Long mesaId,
    @JsonProperty("codigo_cadeira") String codigoCadeira,
    @JsonProperty("coordenadas_x") Double coordenadasX,
    @JsonProperty("coordenadas_y") Double coordenadasY,
    Boolean disponivel
) {}