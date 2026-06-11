package br.com.growup.dto;

public record ElementoEspacial(
    String tipo, // "cadeira", "ar_condicionado", "projetor", "janela"
    int x, 
    int y,
    String descricao
) {}