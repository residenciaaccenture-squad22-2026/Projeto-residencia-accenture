package org.acme;

import java.time.LocalDateTime;
import java.util.Map;

import org.acme.model.StatusRecurso;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ApiResourceTest {
    @Test
    void testStatusEndpoint() {
        given()
          .when().get("/status")
          .then()
             .statusCode(200)
             .body("status", is("online"))
             .body("nome", is("residencia-accenture-api"));
    }

    @Test
    void deveCriarReservaQuandoSalaEEquipamentoEstaoDisponiveis() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoId = criarEquipamento(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 1, 10, 0);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
                        "equipamentoId", equipamentoId,
                        "responsavel", "Maria Silva",
                        "dataHoraInicio", inicio.toString(),
                        "dataHoraFim", fim.toString()))
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .body("sala.id", is(salaId.intValue()))
                .body("equipamento.id", is(equipamentoId.intValue()))
                .body("responsavel", is("Maria Silva"))
                .body("status", is("ATIVA"));
    }

    @Test
    void naoDevePermitirReservaComConflitoDeSala() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 2, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 2, 15, 0);

        criarReserva(salaId, null, inicio, fim);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
                        "responsavel", "Joao Santos",
                        "dataHoraInicio", inicio.plusMinutes(30).toString(),
                        "dataHoraFim", fim.plusMinutes(30).toString()))
                .when().post("/reservas")
                .then()
                .statusCode(400)
                .body("mensagem", is("Sala indisponivel no periodo informado"));
    }

    @Test
    void deveIndicarSalaIndisponivelQuandoStatusNaoPermiteUso() {
        Long salaId = criarSala(StatusRecurso.MANUTENCAO);

        given()
                .queryParam("inicio", "2026-06-03T09:00:00")
                .queryParam("fim", "2026-06-03T10:00:00")
                .when().get("/reservas/disponibilidade/sala/{salaId}", salaId)
                .then()
                .statusCode(200)
                .body("disponivel", is(false))
                .body("mensagem", is("Sala indisponivel no periodo informado"));
    }

    @Test
    void naoDeveCadastrarSalaSemNome() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "capacidade", 8,
                        "localizacao", "Bloco A",
                        "status", StatusRecurso.DISPONIVEL.name()))
                .when().post("/salas")
                .then()
                .statusCode(400)
                .body("mensagem", is("Nome da sala e obrigatorio"));
    }

    @Test
    void naoDeveCadastrarEquipamentoSemTipo() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", "Projetor sem tipo",
                        "descricao", "Cadastro incompleto",
                        "status", StatusRecurso.DISPONIVEL.name()))
                .when().post("/equipamentos")
                .then()
                .statusCode(400)
                .body("mensagem", is("Tipo do equipamento e obrigatorio"));
    }

    @Test
    void naoDeveCriarReservaComPeriodoInvalido() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
                        "responsavel", "Carlos Lima",
                        "dataHoraInicio", "2026-06-04T11:00:00",
                        "dataHoraFim", "2026-06-04T10:00:00"))
                .when().post("/reservas")
                .then()
                .statusCode(400)
                .body("mensagem", is("Data e hora de inicio deve ser anterior ao fim"));
    }

    @Test
    void deveLiberarSalaAposCancelarReserva() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 5, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 5, 10, 0);

        Long reservaId = criarReserva(salaId, null, inicio, fim);

        given()
                .when().put("/reservas/{id}/cancelar", reservaId)
                .then()
                .statusCode(200)
                .body("status", is("CANCELADA"));

        given()
                .queryParam("inicio", inicio.toString())
                .queryParam("fim", fim.toString())
                .when().get("/reservas/disponibilidade/sala/{salaId}", salaId)
                .then()
                .statusCode(200)
                .body("disponivel", is(true))
                .body("mensagem", is("Sala disponivel"));
    }

    @Test
    void deveRetornarNotFoundParaSalaInexistenteNaDisponibilidade() {
        given()
                .queryParam("inicio", "2026-06-06T09:00:00")
                .queryParam("fim", "2026-06-06T10:00:00")
                .when().get("/reservas/disponibilidade/sala/{salaId}", 999999)
                .then()
                .statusCode(404)
                .body("mensagem", is("Sala nao encontrada"));
    }

    @Test
    void deveDisponibilizarDocumentacaoOpenApi() {
        given()
                .when().get("/openapi")
                .then()
                .statusCode(200);
    }

    @Test
    void deveDisponibilizarHealthCheck() {
        given()
                .when().get("/q/health")
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    private Long criarSala(StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", "Sala " + System.nanoTime(),
                        "capacidade", 8,
                        "localizacao", "Bloco A",
                        "status", status.name()))
                .when().post("/salas")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarEquipamento(StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", "Projetor " + System.nanoTime(),
                        "descricao", "Projetor HDMI",
                        "tipo", "VIDEO",
                        "status", status.name()))
                .when().post("/equipamentos")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarReserva(Long salaId, Long equipamentoId, LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("salaId", salaId);
        body.put("responsavel", "Ana Costa");
        body.put("dataHoraInicio", inicio.toString());
        body.put("dataHoraFim", fim.toString());
        if (equipamentoId != null) {
            body.put("equipamentoId", equipamentoId);
        }

        Number id = given()
                .contentType("application/json")
                .body(body)
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }
}
