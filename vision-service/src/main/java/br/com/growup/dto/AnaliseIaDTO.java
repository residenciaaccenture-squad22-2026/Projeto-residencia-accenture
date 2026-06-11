package br.com.growup.dto.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AnaliseIaDTO(
    Long id,
    @JsonProperty("imagem_id") Long imagemId,
    @JsonProperty("metadados_objetos") Map<String, Object> metadadosObjetos, // jsonb no banco
    @JsonProperty("contagem_cadeiras_total") Integer contagemCadeirasTotal,
    @JsonProperty("contagem_recursos_detectados") Integer contagemRecursosDetectados,
    @JsonProperty("precisao_analise") Double precisaoAnalise
) {}