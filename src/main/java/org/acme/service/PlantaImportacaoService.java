package org.acme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.acme.dto.EquipamentoDetectado;
import org.acme.dto.PlantaAnaliseResponse;
import org.acme.dto.PlantaImagemRequest;
import org.acme.dto.PlantaImportacaoResponse;
import org.acme.dto.PosicaoDetectada;
import org.acme.dto.PosicaoResponse;
import org.acme.dto.SalaDetectada;
import org.acme.dto.SalaResponse;
import org.acme.model.Equipamento;
import org.acme.model.Posicao;
import org.acme.model.Sala;
import org.acme.model.StatusRecurso;
import org.acme.repository.EquipamentoRepository;
import org.acme.repository.PosicaoRepository;
import org.acme.repository.SalaRepository;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class PlantaImportacaoService {

    @Inject
    OpenAiPlantaVisionClient visionClient;

    @Inject
    PosicaoRepository posicaoRepository;

    @Inject
    SalaRepository salaRepository;

    @Inject
    EquipamentoRepository equipamentoRepository;

    public PlantaAnaliseResponse analisar(PlantaImagemRequest request) {
        return visionClient.analisar(request);
    }

    public PlantaImportacaoResponse analisarEImportar(PlantaImagemRequest request) {
        PlantaAnaliseResponse analise = analisar(request);
        if (!request.isCadastrar()) {
            PlantaImportacaoResponse response = new PlantaImportacaoResponse();
            response.setAnalise(analise);
            return response;
        }

        return importarResultado(analise);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "salas")
    @CacheInvalidateAll(cacheName = "posicoes")
    @CacheInvalidateAll(cacheName = "equipamentos")
    public PlantaImportacaoResponse importarResultado(PlantaAnaliseResponse analise) {
        validarAnalise(analise);

        PlantaImportacaoResponse response = new PlantaImportacaoResponse();
        response.setAnalise(analise);
        List<PosicaoDetectada> posicoesDetectadas = posicoesOuVazio(analise);

        Sala sala = importarSala(analise.getSala(), posicoesDetectadas.size(), response);
        if (sala != null) {
            salaRepository.flush();
            int equipamentosDaSalaCriados = cadastrarEquipamentosSala(sala, analise.getSala().getEquipamentos());
            response.setEquipamentosCadastrados(response.getEquipamentosCadastrados() + equipamentosDaSalaCriados);
            response.setSala(SalaResponse.from(sala));
        }

        List<Posicao> posicoesPersistidas = new ArrayList<>();
        for (PosicaoDetectada detectada : posicoesDetectadas) {
            if (detectada.getCodigo() == null || detectada.getCodigo().isBlank()) {
                continue;
            }

            Posicao posicao = posicaoRepository.buscarPorCodigo(detectada.getCodigo().trim());
            if (posicao == null) {
                posicao = new Posicao();
                posicao.setCodigo(detectada.getCodigo().trim());
                preencherPosicao(posicao, detectada, sala);
                posicaoRepository.persist(posicao);
                response.setPosicoesCadastradas(response.getPosicoesCadastradas() + 1);
            } else {
                preencherPosicao(posicao, detectada, sala);
                response.setPosicoesAtualizadas(response.getPosicoesAtualizadas() + 1);
            }

            posicaoRepository.flush();
            int equipamentosCriados = cadastrarEquipamentos(posicao, detectada.getEquipamentos());
            response.setEquipamentosCadastrados(response.getEquipamentosCadastrados() + equipamentosCriados);
            posicoesPersistidas.add(posicao);
        }

        response.setPosicoes(posicoesPersistidas.stream()
                .map(PosicaoResponse::from)
                .toList());

        return response;
    }

    private Sala importarSala(SalaDetectada detectada, int quantidadePosicoes, PlantaImportacaoResponse response) {
        if (detectada == null || detectada.getNome() == null || detectada.getNome().isBlank()) {
            return null;
        }

        String nome = detectada.getNome().trim();
        Sala sala = salaRepository.buscarPorNome(nome);
        if (sala == null) {
            sala = new Sala();
            sala.setNome(nome);
            preencherSala(sala, detectada, quantidadePosicoes);
            salaRepository.persist(sala);
            response.setSalasCadastradas(response.getSalasCadastradas() + 1);
        } else {
            preencherSala(sala, detectada, quantidadePosicoes);
            response.setSalasAtualizadas(response.getSalasAtualizadas() + 1);
        }

        return sala;
    }

    private void preencherSala(Sala sala, SalaDetectada detectada, int quantidadePosicoes) {
        sala.setNome(textoOuPadrao(detectada.getNome(), "Sala importada da planta"));
        sala.setCapacidade(detectada.getCapacidade() > 0 ? detectada.getCapacidade() : Math.max(quantidadePosicoes, 1));
        sala.setLocalizacao(textoOuPadrao(detectada.getLocalizacao(), "Nao informada"));
        sala.setStatus(StatusRecurso.DISPONIVEL);
    }

    private void validarAnalise(PlantaAnaliseResponse analise) {
        if (analise == null) {
            throw new BadRequestException("Analise da planta e obrigatoria");
        }

        boolean temSala = analise.getSala() != null
                && analise.getSala().getNome() != null
                && !analise.getSala().getNome().isBlank();
        boolean temPosicoes = analise.getPosicoes() != null && !analise.getPosicoes().isEmpty();

        if (!temSala && !temPosicoes) {
            throw new BadRequestException("Analise da planta deve conter sala ou posicoes");
        }
    }

    private List<PosicaoDetectada> posicoesOuVazio(PlantaAnaliseResponse analise) {
        if (analise.getPosicoes() == null) {
            return List.of();
        }
        return analise.getPosicoes();
    }

    private void preencherPosicao(Posicao posicao, PosicaoDetectada detectada, Sala sala) {
        posicao.setDescricao(textoOuPadrao(detectada.getDescricao(), "Posicao importada da planta"));
        posicao.setLocalizacao(textoOuPadrao(detectada.getLocalizacao(), localizacaoPadrao(sala)));
        posicao.setRecursos(resolverRecursos(detectada));
        posicao.setStatus(StatusRecurso.DISPONIVEL);
    }

    private String localizacaoPadrao(Sala sala) {
        if (sala == null) {
            return "Nao informada";
        }

        if (sala.getLocalizacao() == null || sala.getLocalizacao().isBlank()) {
            return sala.getNome();
        }

        return sala.getLocalizacao() + " - " + sala.getNome();
    }

    private int cadastrarEquipamentos(Posicao posicao, List<EquipamentoDetectado> equipamentos) {
        if (equipamentos == null || equipamentos.isEmpty()) {
            return 0;
        }

        int criados = 0;
        for (EquipamentoDetectado detectado : equipamentos) {
            String nome = textoOuPadrao(detectado.getNome(), null);
            if (nome == null) {
                continue;
            }

            String tipo = normalizarTipoEquipamento(detectado.getTipo());
            if (equipamentoRepository.existePorPosicaoNomeTipo(posicao.getId(), nome, tipo)) {
                continue;
            }

            Equipamento equipamento = new Equipamento();
            equipamento.setPosicao(posicao);
            equipamento.setNome(nome);
            equipamento.setTipo(tipo);
            equipamento.setDescricao(textoOuPadrao(detectado.getDescricao(), "Equipamento importado da planta"));
            equipamento.setStatus(StatusRecurso.DISPONIVEL);
            equipamentoRepository.persist(equipamento);
            posicao.getEquipamentos().add(equipamento);
            criados++;
        }

        return criados;
    }

    private int cadastrarEquipamentosSala(Sala sala, List<EquipamentoDetectado> equipamentos) {
        if (equipamentos == null || equipamentos.isEmpty()) {
            return 0;
        }

        int criados = 0;
        for (EquipamentoDetectado detectado : equipamentos) {
            String nome = textoOuPadrao(detectado.getNome(), null);
            if (nome == null) {
                continue;
            }

            String tipo = normalizarTipoEquipamento(detectado.getTipo());
            if (equipamentoRepository.existePorSalaNomeTipo(sala.getId(), nome, tipo)) {
                continue;
            }

            Equipamento equipamento = new Equipamento();
            equipamento.setSala(sala);
            equipamento.setNome(nome);
            equipamento.setTipo(tipo);
            equipamento.setDescricao(textoOuPadrao(detectado.getDescricao(), "Equipamento da sala importado da planta"));
            equipamento.setStatus(StatusRecurso.DISPONIVEL);
            equipamentoRepository.persist(equipamento);
            sala.getEquipamentos().add(equipamento);
            criados++;
        }

        return criados;
    }

    private String resolverRecursos(PosicaoDetectada detectada) {
        String recursos = textoOuPadrao(detectada.getRecursos(), null);
        if (recursos != null) {
            return recursos;
        }

        if (detectada.getEquipamentos() == null || detectada.getEquipamentos().isEmpty()) {
            return "Nao informado";
        }

        return detectada.getEquipamentos().stream()
                .map(EquipamentoDetectado::getNome)
                .filter(nome -> nome != null && !nome.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String textoOuPadrao(String valor, String padrao) {
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        return valor.trim();
    }

    private String normalizarTipoEquipamento(String tipo) {
        String valor = textoOuPadrao(tipo, "OUT")
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (valor) {
            case "MON", "MONITOR", "TELA" -> "MON";
            case "CAD", "CADEIRA" -> "CAD";
            case "NB", "NOTEBOOK", "LAPTOP" -> "NB";
            case "DOC", "DOCK" -> "DOC";
            case "MDG", "MESA_DIGITALIZADORA" -> "MDG";
            case "PRO", "PROJETOR" -> "PRO";
            case "QDR", "QUADRO", "LOUSA" -> "QDR";
            case "TEL", "TELEFONE" -> "TEL";
            case "MES", "MESA" -> "MES";
            case "TV" -> "TV";
            default -> "OUT";
        };
    }
}
