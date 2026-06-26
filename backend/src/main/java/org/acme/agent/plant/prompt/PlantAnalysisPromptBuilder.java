package org.acme.agent.plant.prompt;

import java.util.stream.Collectors;

import org.acme.agent.plant.dto.PlantAnalysisRequestDTO;
import org.acme.agent.plant.service.PlantAnalysisDictionary;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlantAnalysisPromptBuilder {

    public String build(PlantAnalysisRequestDTO request) {
        String roomHint = request.getRoomNameHint() == null || request.getRoomNameHint().isBlank()
                ? "Nao informado"
                : request.getRoomNameHint().trim();
        String observation = request.getObservacao() == null || request.getObservacao().isBlank()
                ? "Nao informada"
                : request.getObservacao().trim();

        return """
                Voce e um extrator visual de dados para salas corporativas.
                Retorne somente JSON valido. Nao retorne markdown, comentarios, texto antes ou depois do JSON.
                Nao salve, nao invente, nao estime alem do que a imagem permitir.
                Sempre marque revisao como true.
                Nunca use campos descricao, description ou texto livre para identificar posicao.

                Contexto informado pelo usuario:
                - sala sugerida: %s
                - observacao: %s

                Tarefa:
                1. Identifique posicoes visiveis na sala/planta.
                2. Use codigos visiveis quando existirem; se nao existirem, use P001, P002, P003 em ordem de leitura.
                3. Informe linha aproximada em lin e coluna aproximada em col.
                4. Informe coordenada aproximada da posicao na imagem em coord.x e coord.y.
                5. Identifique equipamentos por posicao usando somente codigos padronizados.
                6. Informe quantidade q de cada equipamento e confianca c.
                7. Se nao tiver certeza, use confianca B.
                8. Se o equipamento nao estiver no dicionario, use OUT.
                9. Recalcule resumoEq por tipo de equipamento.
                10. Use obs para avisos curtos sobre baixa qualidade, oclusao ou necessidade de revisao.

                Equipamentos permitidos: %s.
                Confianca permitida: A=Alta, M=Media, B=Baixa.

                Formato exato:
                {
                  "sala":"Laboratorio 01",
                  "totalPos":1,
                  "pos":[{"cod":"P001","lin":1,"col":1,"coord":{"x":120,"y":340},"eq":[{"t":"MON","q":1,"c":"A"}],"conf":"A"}],
                  "resumoEq":[{"t":"MON","qtd":1}],
                  "confGeral":"M",
                  "revisao":true,
                  "obs":["Recomenda-se revisao humana antes de salvar no banco."]
                }
                """.formatted(roomHint, observation, equipmentCodes());
    }

    private String equipmentCodes() {
        return PlantAnalysisDictionary.EQUIPMENT_TYPES.stream()
                .sorted()
                .collect(Collectors.joining(", "));
    }
}