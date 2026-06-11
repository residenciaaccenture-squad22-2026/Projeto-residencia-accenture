package br.com.growup.dto;

import java.util.List;

public record AnaliseResponse(
    List<ElementoEspacial> assentos,
    List<ElementoEspacial> recursosFixos
) {}