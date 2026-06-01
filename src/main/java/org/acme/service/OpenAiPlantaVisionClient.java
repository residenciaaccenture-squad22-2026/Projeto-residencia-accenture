package org.acme.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import org.acme.dto.PlantaAnaliseResponse;
import org.acme.dto.PlantaImagemRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class OpenAiPlantaVisionClient {

    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final int MAX_IMAGE_BYTES = 15 * 1024 * 1024;

    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "planta.reconhecimento.model", defaultValue = "gpt-4.1-mini")
    String model;

    @ConfigProperty(name = "planta.reconhecimento.max-output-tokens", defaultValue = "1800")
    int maxOutputTokens;

    @ConfigProperty(name = "planta.reconhecimento.image-detail", defaultValue = "low")
    String imageDetail;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public PlantaAnaliseResponse analisar(PlantaImagemRequest request) {
        validarRequest(request);

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("placeholder")) {
            throw new WebApplicationException("OPENAI_API_KEY nao configurada para analisar a planta",
                    Response.Status.SERVICE_UNAVAILABLE);
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_RESPONSES_URL))
                    .timeout(Duration.ofSeconds(90))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(montarPayload(request).toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new WebApplicationException("Falha ao analisar planta: " + response.body(),
                        Response.Status.BAD_GATEWAY);
            }

            String outputText = extrairTextoResposta(objectMapper.readTree(response.body()));
            return objectMapper.readValue(outputText, PlantaAnaliseResponse.class);
        } catch (IOException exception) {
            throw new WebApplicationException("Erro ao processar resposta da IA", exception,
                    Response.Status.BAD_GATEWAY);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Analise da planta interrompida", exception,
                    Response.Status.BAD_GATEWAY);
        }
    }

    private ObjectNode montarPayload(PlantaImagemRequest request) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("max_output_tokens", maxOutputTokens);

        ArrayNode input = payload.putArray("input");
        input.add(mensagem("developer", promptSistema()));

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");
        content.add(inputText(
                "Analise esta foto/planta e extraia a sala/ambiente, posicoes de trabalho e equipamentos visiveis."));
        ObjectNode image = objectMapper.createObjectNode();
        image.put("type", "input_image");
        image.put("detail", normalizarDetalheImagem());
        image.put("image_url", dataUrl(request));
        content.add(image);
        input.add(userMessage);

        ObjectNode text = payload.putObject("text");
        ObjectNode format = text.putObject("format");
        format.put("type", "json_schema");
        format.put("name", "planta_posicoes_detectadas");
        format.put("strict", true);
        format.set("schema", schemaAnalise());

        return payload;
    }

    private ObjectNode mensagem(String role, String text) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        ArrayNode content = message.putArray("content");
        content.add(inputText(text));
        return message;
    }

    private ObjectNode inputText(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "input_text");
        node.put("text", text);
        return node;
    }

    private String promptSistema() {
        return """
                Extraia de plantas/fotos de escritorio: sala, posicoes e equipamentos.
                Seja curto. Nao explique processo.
                Se sala sem nome visivel, use "Sala importada".
                Se posicao sem rotulo, use P01, P02, P03 em ordem de leitura.
                Capacidade da sala = quantidade de posicoes, se nao houver valor explicito.
                Equipamento compartilhado fica em sala.equipamentos; equipamento de mesa fica na posicao.
                Nao invente itens. Use confianca baixa em duvida.
                equipamentos.tipo aceita somente: MON, CAD, NB, DOC, MDG, PRO, TV, QDR, TEL, MES, OUT.
                """;
    }

    private ObjectNode schemaAnalise() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("resumo", stringSchema());
        properties.set("observacoes", stringSchema());
        properties.set("sala", salaSchema());
        properties.set("posicoes", posicoesArraySchema());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);
        root.set("required", array("resumo", "observacoes", "sala", "posicoes"));
        root.set("properties", properties);
        return root;
    }

    private ObjectNode salaSchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("nome", stringSchema());
        properties.set("descricao", stringSchema());
        properties.set("localizacao", stringSchema());
        properties.set("capacidade", integerSchema());
        properties.set("confianca", numberSchema());
        properties.set("equipamentos", equipamentosArraySchema());

        ObjectNode sala = objectMapper.createObjectNode();
        sala.put("type", "object");
        sala.put("additionalProperties", false);
        sala.set("required", array("nome", "descricao", "localizacao", "capacidade", "confianca", "equipamentos"));
        sala.set("properties", properties);
        return sala;
    }

    private ObjectNode posicoesArraySchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("codigo", stringSchema());
        properties.set("descricao", stringSchema());
        properties.set("localizacao", stringSchema());
        properties.set("recursos", stringSchema());
        properties.set("confianca", numberSchema());
        properties.set("equipamentos", equipamentosArraySchema());

        ObjectNode item = objectMapper.createObjectNode();
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", array("codigo", "descricao", "localizacao", "recursos", "confianca", "equipamentos"));
        item.set("properties", properties);

        ObjectNode array = objectMapper.createObjectNode();
        array.put("type", "array");
        array.set("items", item);
        return array;
    }

    private ObjectNode equipamentosArraySchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("nome", stringSchema());
        properties.set("tipo", tipoEquipamentoSchema());
        properties.set("descricao", stringSchema());
        properties.set("confianca", numberSchema());

        ObjectNode item = objectMapper.createObjectNode();
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", array("nome", "tipo", "descricao", "confianca"));
        item.set("properties", properties);

        ObjectNode array = objectMapper.createObjectNode();
        array.put("type", "array");
        array.set("items", item);
        return array;
    }

    private ObjectNode stringSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "string");
        return schema;
    }

    private ObjectNode tipoEquipamentoSchema() {
        ObjectNode schema = stringSchema();
        schema.set("enum", array("MON", "CAD", "NB", "DOC", "MDG", "PRO", "TV", "QDR", "TEL", "MES", "OUT"));
        return schema;
    }

    private ObjectNode numberSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "number");
        schema.put("minimum", 0);
        schema.put("maximum", 1);
        return schema;
    }

    private ObjectNode integerSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "integer");
        schema.put("minimum", 1);
        return schema;
    }

    private ArrayNode array(String... values) {
        ArrayNode array = objectMapper.createArrayNode();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private String dataUrl(PlantaImagemRequest request) {
        return "data:%s;base64,%s".formatted(normalizarMimeType(request.getMimeType()),
                limparBase64(request.getImagemBase64()));
    }

    private void validarRequest(PlantaImagemRequest request) {
        if (request == null || request.getImagemBase64() == null || request.getImagemBase64().isBlank()) {
            throw new BadRequestException("Imagem em base64 e obrigatoria");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(limparBase64(request.getImagemBase64()));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Imagem em base64 invalida");
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Imagem da planta excede o tamanho maximo permitido");
        }
    }

    private String limparBase64(String valor) {
        int dataUrlSeparator = valor.indexOf(',');
        if (valor.startsWith("data:") && dataUrlSeparator >= 0) {
            return valor.substring(dataUrlSeparator + 1);
        }
        return valor;
    }

    private String normalizarMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return "image/png";
        }
        return mimeType;
    }

    private String normalizarDetalheImagem() {
        if ("high".equalsIgnoreCase(imageDetail) || "auto".equalsIgnoreCase(imageDetail)) {
            return imageDetail.toLowerCase();
        }
        return "low";
    }

    private String extrairTextoResposta(JsonNode root) {
        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText();
        }

        JsonNode output = root.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.get("text");
                        if (text != null && text.isTextual()) {
                            return text.asText();
                        }
                    }
                }
            }
        }

        throw new WebApplicationException("Resposta da IA nao contem JSON de analise", Response.Status.BAD_GATEWAY);
    }
}
