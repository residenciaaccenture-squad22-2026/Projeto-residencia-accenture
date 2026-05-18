package org.acme;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.acme.model.StatusRecurso;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void naoDevePermitirReservaComConflitoDeEquipamento() {
        Long primeiraSalaId = criarSala(StatusRecurso.DISPONIVEL);
        Long segundaSalaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoId = criarEquipamento(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 7, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 7, 10, 0);

        criarReserva(primeiraSalaId, equipamentoId, inicio, fim);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", segundaSalaId,
                        "equipamentoId", equipamentoId,
                        "responsavel", "Joao Santos",
                        "dataHoraInicio", inicio.plusMinutes(15).toString(),
                        "dataHoraFim", fim.plusMinutes(15).toString()))
                .when().post("/reservas")
                .then()
                .statusCode(400)
                .body("mensagem", is("Equipamento indisponivel no periodo informado"));
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
    void deveListarSomenteSalasDisponiveisNoPeriodo() {
        Long salaLivreId = criarSala(StatusRecurso.DISPONIVEL);
        Long salaOcupadaId = criarSala(StatusRecurso.DISPONIVEL);
        Long salaManutencaoId = criarSala(StatusRecurso.MANUTENCAO);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 8, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 8, 10, 0);

        criarReserva(salaOcupadaId, null, inicio, fim);

        List<Integer> ids = given()
                .queryParam("inicio", inicio.toString())
                .queryParam("fim", fim.toString())
                .when().get("/disponibilidade/salas")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertTrue(ids.contains(salaLivreId.intValue()));
        assertFalse(ids.contains(salaOcupadaId.intValue()));
        assertFalse(ids.contains(salaManutencaoId.intValue()));
    }

    @Test
    void deveListarSomenteEquipamentosDisponiveisNoPeriodo() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoLivreId = criarEquipamento(StatusRecurso.DISPONIVEL);
        Long equipamentoOcupadoId = criarEquipamento(StatusRecurso.DISPONIVEL);
        Long equipamentoManutencaoId = criarEquipamento(StatusRecurso.MANUTENCAO);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 9, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 9, 10, 0);

        criarReserva(salaId, equipamentoOcupadoId, inicio, fim);

        List<Integer> ids = given()
                .queryParam("inicio", inicio.toString())
                .queryParam("fim", fim.toString())
                .when().get("/disponibilidade/equipamentos")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertTrue(ids.contains(equipamentoLivreId.intValue()));
        assertFalse(ids.contains(equipamentoOcupadoId.intValue()));
        assertFalse(ids.contains(equipamentoManutencaoId.intValue()));
    }

    @Test
    void naoDeveAtualizarReservaCancelada() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long reservaId = criarReserva(
                salaId,
                null,
                LocalDateTime.of(2026, 6, 10, 9, 0),
                LocalDateTime.of(2026, 6, 10, 10, 0));

        given()
                .when().put("/reservas/{id}/cancelar", reservaId)
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
                        "responsavel", "Reserva Editada",
                        "dataHoraInicio", "2026-06-10T11:00:00",
                        "dataHoraFim", "2026-06-10T12:00:00"))
                .when().put("/reservas/{id}", reservaId)
                .then()
                .statusCode(400)
                .body("mensagem", is("Reserva cancelada nao pode ser atualizada"));
    }

    @Test
    void naoDeveAtualizarReservaGerandoConflitoDeSala() {
        Long salaOriginalId = criarSala(StatusRecurso.DISPONIVEL);
        Long salaOcupadaId = criarSala(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 11, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 11, 10, 0);

        criarReserva(salaOcupadaId, null, inicio, fim);
        Long reservaId = criarReserva(salaOriginalId, null, inicio.plusHours(2), fim.plusHours(2));

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaOcupadaId,
                        "responsavel", "Reserva Conflitante",
                        "dataHoraInicio", inicio.plusMinutes(30).toString(),
                        "dataHoraFim", fim.plusMinutes(30).toString()))
                .when().put("/reservas/{id}", reservaId)
                .then()
                .statusCode(400)
                .body("mensagem", is("Sala indisponivel no periodo informado"));
    }

    @Test
    void naoDeveRemoverSalaComReservaVinculada() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        criarReserva(
                salaId,
                null,
                LocalDateTime.of(2026, 6, 12, 9, 0),
                LocalDateTime.of(2026, 6, 12, 10, 0));

        given()
                .when().delete("/salas/{id}", salaId)
                .then()
                .statusCode(409)
                .body("mensagem", is("Sala possui reservas vinculadas"));
    }

    @Test
    void naoDeveRemoverEquipamentoComReservaVinculada() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoId = criarEquipamento(StatusRecurso.DISPONIVEL);
        criarReserva(
                salaId,
                equipamentoId,
                LocalDateTime.of(2026, 6, 13, 9, 0),
                LocalDateTime.of(2026, 6, 13, 10, 0));

        given()
                .when().delete("/equipamentos/{id}", equipamentoId)
                .then()
                .statusCode(409)
                .body("mensagem", is("Equipamento possui reservas vinculadas"));
    }

    @Test
    void naoDeveAceitarStatusInvalidoNoCadastroDeSala() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "nome": "Sala status invalido",
                          "capacidade": 8,
                          "localizacao": "Bloco A",
                          "status": "BLOQUEADA"
                        }
                        """)
                .when().post("/salas")
                .then()
                .statusCode(400);
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
