package br.com.growup.dto;

public record AlocacaoRequest(
    Long funcionarioId, 
    String imageUrl
) {}