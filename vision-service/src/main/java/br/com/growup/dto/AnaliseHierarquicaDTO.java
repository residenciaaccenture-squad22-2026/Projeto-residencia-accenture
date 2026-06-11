package br.com.growup.dto;

import java.util.List;

public record AnaliseHierarquicaDTO(
    String nomeSugeridoSala,
    List<MesaIa> mesas
) {
    public record MesaIa(
        String idFicticioMesa,
        String codigoMesa,
        List<PosicaoIa> posicoes
    ) {}

    public record PosicaoIa(
        String idFicticioPosicao,
        String codigoCadeira,
        double coordenadasX,
        double coordenadasY,
        List<RecursoIa> recursos
    ) {}

    public record RecursoIa(
        String categoria,
        String nomeModelo
    ) {}
}