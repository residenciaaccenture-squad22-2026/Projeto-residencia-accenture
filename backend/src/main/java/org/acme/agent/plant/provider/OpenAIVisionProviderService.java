package org.acme.agent.plant.provider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.acme.agent.plant.dto.ProcessedImageDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class OpenAIVisionProviderService implements AIProviderService {

    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String CONFIGURATION_ERROR =
            "IA visual nao configurada. Configure AI_PROVIDER, AI_API_KEY, AI_MODEL e AI_VISION_ENABLED.";

    @ConfigProperty(name = "ai.provider")
    Optional<String> provider;

    @ConfigProperty(name = "ai.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "ai.model")
    Optional<String> model;

    @ConfigProperty(name = "ai.vision-enabled", defaultValue = "false")
    boolean visionEnabled;

    @ConfigProperty(name = "ai.vision.timeout-seconds", defaultValue = "90")
    long timeoutSeconds;

    @ConfigProperty(name = "ai.vision.max-output-tokens", defaultValue = "4000")
    int maxOutputTokens;

    @ConfigProperty(name = "ai.vision.image-detail", defaultValue = "high")
    String imageDetail;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    public String analyzeImage(ProcessedImageDTO image, String prompt) {
        validateConfiguration();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_RESPONSES_URL))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey.orElseThrow())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildPayload(image, prompt).toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new WebApplicationException("Chave de IA invalida ou sem permissao", Response.Status.BAD_GATEWAY);
            }
            if (response.statusCode() == 408 || response.statusCode() == 429) {
                throw new WebApplicationException("Timeout ou limite do provedor de IA", Response.Status.BAD_GATEWAY);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new WebApplicationException("Erro de comunicacao com provedor de IA", Response.Status.BAD_GATEWAY);
            }

            return extractOutputText(objectMapper.readTree(response.body()));
        } catch (IOException exception) {
            throw new WebApplicationException("Erro ao processar resposta da IA", exception, Response.Status.BAD_GATEWAY);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WebApplicationException("Timeout da IA visual", exception, Response.Status.BAD_GATEWAY);
        }
    }

    private void validateConfiguration() {
        if (!visionEnabled
                || !hasText(provider)
                || !"openai".equalsIgnoreCase(provider.orElse(""))
                || !hasText(apiKey)
                || !hasText(model)
                || containsPlaceholder(apiKey.orElse(""))) {
            throw new WebApplicationException(CONFIGURATION_ERROR, Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    private ObjectNode buildPayload(ProcessedImageDTO image, String prompt) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model.orElseThrow());
        payload.put("max_output_tokens", maxOutputTokens);

        ArrayNode input = payload.putArray("input");
        input.add(message("developer", prompt));

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");
        content.add(inputText("Analise a imagem e retorne somente o JSON estruturado solicitado."));

        ObjectNode imageNode = objectMapper.createObjectNode();
        imageNode.put("type", "input_image");
        imageNode.put("detail", normalizeImageDetail());
        imageNode.put("image_url", image.getImageReference());
        content.add(imageNode);
        input.add(userMessage);

        ObjectNode text = payload.putObject("text");
        ObjectNode format = text.putObject("format");
        format.put("type", "json_schema");
        format.put("name", "plant_analysis_result");
        format.put("strict", true);
        format.set("schema", responseSchema());

        return payload;
    }

    private ObjectNode message(String role, String text) {
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

    private ObjectNode responseSchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("sala", stringSchema());
        properties.set("totalPos", integerSchema(0));
        properties.set("pos", positionsSchema());
        properties.set("resumoEq", equipmentSummarySchema());
        properties.set("confGeral", confidenceSchema());
        properties.set("revisao", booleanSchema());
        properties.set("obs", stringArraySchema());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);
        root.set("required", array("sala", "totalPos", "pos", "resumoEq", "confGeral", "revisao", "obs"));
        root.set("properties", properties);
        return root;
    }

    private ObjectNode positionsSchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("cod", stringSchema());
        properties.set("lin", integerSchema(1));
        properties.set("col", integerSchema(1));
        properties.set("coord", coordinateSchema());
        properties.set("eq", equipmentSchema());
        properties.set("conf", confidenceSchema());

        ObjectNode item = objectMapper.createObjectNode();
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", array("cod", "lin", "col", "coord", "eq", "conf"));
        item.set("properties", properties);

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", item);
        return schema;
    }

    private ObjectNode coordinateSchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("x", integerSchema(0));
        properties.set("y", integerSchema(0));

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.set("required", array("x", "y"));
        schema.set("properties", properties);
        return schema;
    }

    private ObjectNode equipmentSchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("t", equipmentTypeSchema());
        properties.set("q", integerSchema(1));
        properties.set("c", confidenceSchema());

        ObjectNode item = objectMapper.createObjectNode();
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", array("t", "q", "c"));
        item.set("properties", properties);

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", item);
        return schema;
    }

    private ObjectNode equipmentSummarySchema() {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("t", equipmentTypeSchema());
        properties.set("qtd", integerSchema(0));

        ObjectNode item = objectMapper.createObjectNode();
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", array("t", "qtd"));
        item.set("properties", properties);

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", item);
        return schema;
    }

    private ObjectNode stringSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "string");
        return schema;
    }

    private ObjectNode stringArraySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        schema.set("items", stringSchema());
        return schema;
    }

    private ObjectNode integerSchema(int minimum) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "integer");
        schema.put("minimum", minimum);
        return schema;
    }

    private ObjectNode booleanSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "boolean");
        return schema;
    }

    private ObjectNode equipmentTypeSchema() {
        ObjectNode schema = stringSchema();
        schema.set("enum", array("MON", "TEC", "MOU", "CPU", "NOT", "CAD", "MES", "PRO", "TOM", "OUT"));
        return schema;
    }

    private ObjectNode confidenceSchema() {
        ObjectNode schema = stringSchema();
        schema.set("enum", array("A", "M", "B"));
        return schema;
    }

    private ArrayNode array(String... values) {
        ArrayNode array = objectMapper.createArrayNode();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private String extractOutputText(JsonNode root) {
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

        throw new WebApplicationException("Resposta da IA nao contem JSON valido", Response.Status.BAD_GATEWAY);
    }

    private boolean hasText(Optional<String> value) {
        return value.isPresent() && !value.orElse("").isBlank();
    }

    private boolean containsPlaceholder(String value) {
        String normalized = value.toLowerCase();
        return normalized.contains("placeholder")
                || normalized.contains("sua_chave")
                || normalized.contains("seu_");
    }

    private String normalizeImageDetail() {
        if ("low".equalsIgnoreCase(imageDetail) || "auto".equalsIgnoreCase(imageDetail)) {
            return imageDetail.toLowerCase();
        }
        return "high";
    }
}