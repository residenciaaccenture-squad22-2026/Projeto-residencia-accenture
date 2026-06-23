package br.com.growup.dto;

public record SetupConfirmadoRequest(
    String nome,
    String localizacao,
    AnaliseHierarquicaDTO analiseRevisada
) {}