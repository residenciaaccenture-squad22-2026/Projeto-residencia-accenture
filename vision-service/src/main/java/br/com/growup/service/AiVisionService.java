package br.com.growup.service;

import br.com.growup.dto.AnaliseHierarquicaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AiVisionService {

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "ai.api.key")
    String apiKey;

    public AnaliseHierarquicaDTO mapearPlantaHierarquica(String imageUrl) {
        String prompt = "Analise a imagem desta sala de escritório e estruture o layout geometricamente. " +
                "Agrupe as cadeiras detectadas pelas suas respectivas mesas. " +
                "Se houver recursos (como monitor, tomada, impressora ou projetor) próximos a uma cadeira específica, vincule esse recurso a ela. " +
                "Retorne EXATAMENTE e APENAS um objeto JSON válido seguindo esta estrutura, sem markdown: " +
                "{" +
                "  \"nomeSugeridoSala\": \"Sala de Reuniões\"," +
                "  \"mesas\": [" +
                "    {" +
                "      \"idFicticioMesa\": \"mesa_1\"," +
                "      \"codigoMesa\": \"MESA-01\"," +
                "      \"posicoes\": [" +
                "        {" +
                "          \"idFicticioPosicao\": \"posicao_1\"," +
                "          \"codigoCadeira\": \"CAD-01\"," +
                "          \"coordenadasX\": 450.0," +
                "          \"coordenadasY\": 550.0," +
                "          \"recursos\": [" +
                "            {\"categoria\": \"monitor\", \"nomeModelo\": \"Monitor Dell 27 Polegadas\"}" +
                "          ]" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
                "}";

        try {
            String base64Image = baixarEConverterParaBase64(imageUrl);
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                    Map.of("text", prompt),
                    Map.of("inlineData", Map.of("mimeType", "image/jpeg", "data", base64Image))
                )))
            );

            String jsonBody = mapper.writeValueAsString(requestBody);
            String googleUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey.trim();

            // As variáveis que o compilador não estava achando estão sendo criadas aqui:
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(googleUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // A chamada para a API com as travas de segurança
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400) {
                throw new RuntimeException("A API do Gemini retornou um erro (HTTP " + response.statusCode() + "): " + response.body());
            }

            Map<String, Object> aiResponse = mapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) aiResponse.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("O Gemini não retornou dados. Resposta bruta da API: " + response.body());
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String jsonResult = (String) parts.get(0).get("text");

            jsonResult = jsonResult.replace("```json", "").replace("```", "").trim();

            return mapper.readValue(jsonResult, AnaliseHierarquicaDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("Erro na análise da IA: " + e.getMessage(), e);
        }
    }
    
    
    private String baixarEConverterParaBase64(String imageUrl) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();
            
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Falha ao baixar imagem. HTTP Status: " + response.statusCode());
            }
            
            return Base64.getEncoder().encodeToString(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter imagem para Base64: " + e.getMessage(), e);
        }
    }
}