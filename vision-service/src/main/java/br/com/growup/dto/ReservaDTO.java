package br.com.growup.dto.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReservaDTO(
    Long id,
    @JsonProperty("funcionario_id") Long funcionarioId,
    @JsonProperty("posicao_id") Long posicaoId,
    @JsonProperty("data_inicio") String dataInicio,
    @JsonProperty("data_fim") String dataFim,
    String status
) {}