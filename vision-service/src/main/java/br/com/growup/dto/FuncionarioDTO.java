package br.com.growup.dto.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FuncionarioDTO(
    Long id,
    String nome,
    String email,
    String cargo,
    @JsonProperty("posicao_preferencial") String posicaoPreferencial,
    @JsonProperty("created_at") String createdAt
) {}