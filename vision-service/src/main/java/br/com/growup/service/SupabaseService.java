package br.com.growup.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SupabaseService {

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "supabase.url")
    String supabaseUrl;

    @ConfigProperty(name = "supabase.key")
    String supabaseKey;

    public Long criarSala(String nome, String localizacao) {
        // CORRIGIDO: Rota no singular e apenas com os campos da tabela "sala"
        return executarInsert("/rest/v1/sala", Map.of(
            "nome", nome, 
            "localizacao", localizacao
        ));
    }

    public Long criarMesa(Long salaId, String codigoMesa) {
        // Baseado no seu SQL, a tabela é 'mesa'
        return executarInsert("/rest/v1/mesa", Map.of("sala_id", salaId, "codigo_mesa", codigoMesa));
    }

    public Long criarPosicao(Long mesaId, String codigoCadeira, double x, double y) {
        // Baseado no seu SQL, a tabela é 'posicao'
        return executarInsert("/rest/v1/posicao", Map.of(
            "mesa_id", mesaId,
            "codigo_cadeira", codigoCadeira,
            "coordenadas_x", x,
            "coordenadas_y", y,
            "disponivel", true
        ));
    }

    public void criarRecurso(Long posicaoId, String nomeModelo, String categoria) {
        executarInsert("/rest/v1/recurso", Map.of(
            "posicao_id", posicaoId,
            "nome_modelo", nomeModelo,
            "categoria", categoria
        ));
    }

    private Long executarInsert(String rota, Map<String, Object> payload) {
        try {
            String url = supabaseUrl + rota;
            String jsonBody = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Erro ao inserir [" + rota + "]: " + response.body());
            }

            List<Map<String, Object>> res = mapper.readValue(response.body(), new TypeReference<>() {});
            if (!res.isEmpty() && res.get(0).containsKey("id")) {
                return ((Number) res.get(0).get("id")).longValue();
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Falha no Supabase", e);
        }
    }
}