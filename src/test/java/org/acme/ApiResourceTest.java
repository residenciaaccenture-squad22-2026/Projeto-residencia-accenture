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
    void deveCriarReservaQuandoSalaEstaDisponivelComEquipamentos() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoId = criarEquipamento(salaId, StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 1, 10, 0);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
                        "responsavel", "Maria Silva",
                        "dataHoraInicio", inicio.toString(),
                        "dataHoraFim", fim.toString()))
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .body("sala.id", is(salaId.intValue()))
                .body("sala.equipamentos[0].id", is(equipamentoId.intValue()))
                .body("responsavel", is("Maria Silva"))
                .body("status", is("ATIVA"));
    }

    @Test
    void naoDevePermitirReservaComConflitoDeSala() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 2, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 2, 15, 0);

        criarReserva(salaId, inicio, fim);

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
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
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

        Long reservaId = criarReserva(salaId, inicio, fim);

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

        criarReserva(salaOcupadaId, inicio, fim);

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
    void reservaDePosicaoNaoDeveAfetarDisponibilidadeDeSalas() {
        Long salaLivreId = criarSala(StatusRecurso.DISPONIVEL);
        Long posicaoOcupadaId = criarPosicao(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 16, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 16, 10, 0);

        criarReservaPosicao(posicaoOcupadaId, inicio, fim);

        List<Integer> ids = given()
                .queryParam("inicio", inicio.toString())
                .queryParam("fim", fim.toString())
                .when().get("/disponibilidade/salas")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertTrue(ids.contains(salaLivreId.intValue()));
    }

    @Test
    void deveCadastrarUsuario() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", "Marina Alves",
                        "role", "gestor",
                        "cargo", "Product Manager"))
                .when().post("/usuarios")
                .then()
                .statusCode(201)
                .body("nome", is("Marina Alves"))
                .body("role", is("GESTOR"))
                .body("cargo", is("Product Manager"));
    }

    @Test
    void deveCriarReservaDePosicaoQuandoDisponivel() {
        Long usuarioId = criarUsuario("Lucas Freitas", "FUNCIONARIO", "Dev");
        Long posicaoId = criarPosicao(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 13, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 13, 10, 0);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "posicaoId", posicaoId,
                        "usuarioId", usuarioId,
                        "responsavel", "Lucas Freitas",
                        "dataHoraInicio", inicio.toString(),
                        "dataHoraFim", fim.toString()))
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .body("posicao.id", is(posicaoId.intValue()))
                .body("usuario.id", is(usuarioId.intValue()))
                .body("responsavel", is("Lucas Freitas"))
                .body("status", is("ATIVA"));
    }

    @Test
    void naoDevePermitirReservaComConflitoDePosicao() {
        Long posicaoId = criarPosicao(StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 14, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 14, 15, 0);

        criarReservaPosicao(posicaoId, inicio, fim);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "posicaoId", posicaoId,
                        "responsavel", "Joao Santos",
                        "dataHoraInicio", inicio.plusMinutes(30).toString(),
                        "dataHoraFim", fim.plusMinutes(30).toString()))
                .when().post("/reservas")
                .then()
                .statusCode(400)
                .body("mensagem", is("Posicao indisponivel no periodo informado"));
    }

    @Test
    void deveListarSomentePosicoesDisponiveisNoPeriodo() {
        Long posicaoLivreId = criarPosicao(StatusRecurso.DISPONIVEL);
        Long posicaoOcupadaId = criarPosicao(StatusRecurso.DISPONIVEL);
        Long posicaoManutencaoId = criarPosicao(StatusRecurso.MANUTENCAO);
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 15, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 6, 15, 10, 0);

        criarReservaPosicao(posicaoOcupadaId, inicio, fim);

        List<Integer> ids = given()
                .queryParam("inicio", inicio.toString())
                .queryParam("fim", fim.toString())
                .when().get("/disponibilidade/posicoes")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertTrue(ids.contains(posicaoLivreId.intValue()));
        assertFalse(ids.contains(posicaoOcupadaId.intValue()));
        assertFalse(ids.contains(posicaoManutencaoId.intValue()));
    }

    @Test
    void deveCadastrarEquipamentoVinculadoAPosicao() {
        Long posicaoId = criarPosicao(StatusRecurso.DISPONIVEL);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "posicaoId", posicaoId,
                        "nome", "Monitor posicao",
                        "descricao", "Monitor identificado na planta",
                        "tipo", "MONITOR",
                        "status", StatusRecurso.DISPONIVEL.name()))
                .when().post("/equipamentos")
                .then()
                .statusCode(201)
                .body("posicaoId", is(posicaoId.intValue()))
                .body("salaId", is((Object) null))
                .body("tipo", is("MONITOR"));
    }

    @Test
    void deveImportarResultadoDePlantaCadastrandoPosicoesEEquipamentos() {
        Map<String, Object> body = Map.of(
                "resumo", "Planta com duas posicoes",
                "observacoes", "Importacao de teste",
                "posicoes", List.of(
                        Map.of(
                                "codigo", "P-PLANTA-01",
                                "descricao", "Posicao proxima a janela",
                                "localizacao", "Bloco C",
                                "recursos", "Monitor e dock",
                                "confianca", 0.95,
                                "equipamentos", List.of(
                                        Map.of(
                                                "nome", "Monitor Dell",
                                                "tipo", "MONITOR",
                                                "descricao", "Monitor sobre a mesa",
                                                "confianca", 0.92),
                                        Map.of(
                                                "nome", "Dock USB-C",
                                                "tipo", "DOCK",
                                                "descricao", "Dock ao lado do monitor",
                                                "confianca", 0.88))),
                        Map.of(
                                "codigo", "P-PLANTA-02",
                                "descricao", "Posicao central",
                                "localizacao", "Bloco C",
                                "recursos", "Cadeira ergonomica",
                                "confianca", 0.9,
                                "equipamentos", List.of(
                                        Map.of(
                                                "nome", "Cadeira ergonomica",
                                                "tipo", "CADEIRA",
                                                "descricao", "Cadeira na posicao",
                                                "confianca", 0.9)))));

        given()
                .contentType("application/json")
                .body(body)
                .when().post("/plantas/importar/resultado")
                .then()
                .statusCode(200)
                .body("posicoesCadastradas", is(2))
                .body("equipamentosCadastrados", is(3))
                .body("posicoes[0].codigo", is("P-PLANTA-01"))
                .body("posicoes[0].equipamentos.size()", is(2))
                .body("posicoes[0].equipamentos[0].tipo", is("MON"))
                .body("posicoes[0].equipamentos[1].tipo", is("DOC"))
                .body("posicoes[1].equipamentos[0].tipo", is("CAD"));

        List<String> codigos = given()
                .when().get("/posicoes")
                .then()
                .statusCode(200)
                .extract().path("codigo");

        assertTrue(codigos.contains("P-PLANTA-01"));
        assertTrue(codigos.contains("P-PLANTA-02"));
    }

    @Test
    void deveImportarResultadoDePlantaCadastrandoSalaPosicoesEEquipamentos() {
        Map<String, Object> body = Map.of(
                "resumo", "Planta com sala, posicao e equipamentos",
                "observacoes", "Importacao completa de teste",
                "sala", Map.of(
                        "nome", "Sala Planta " + System.nanoTime(),
                        "descricao", "Sala identificada pela planta",
                        "localizacao", "Bloco D",
                        "capacidade", 1,
                        "confianca", 0.93,
                        "equipamentos", List.of(
                                Map.of(
                                        "nome", "Projetor sala",
                                        "tipo", "PRO",
                                        "descricao", "Projetor fixo no teto",
                                        "confianca", 0.9))),
                "posicoes", List.of(
                        Map.of(
                                "codigo", "P-SALA-PLANTA-" + System.nanoTime(),
                                "descricao", "Posicao dentro da sala importada",
                                "localizacao", "",
                                "recursos", "Monitor e dock",
                                "confianca", 0.91,
                                "equipamentos", List.of(
                                        Map.of(
                                                "nome", "Monitor posicao sala",
                                                "tipo", "MON",
                                                "descricao", "Monitor sobre a mesa",
                                                "confianca", 0.88)))));

        Number salaId = given()
                .contentType("application/json")
                .body(body)
                .when().post("/plantas/importar/resultado")
                .then()
                .statusCode(200)
                .body("salasCadastradas", is(1))
                .body("posicoesCadastradas", is(1))
                .body("equipamentosCadastrados", is(2))
                .body("sala.equipamentos[0].tipo", is("PRO"))
                .body("posicoes[0].equipamentos[0].tipo", is("MON"))
                .extract().path("sala.id");

        List<Integer> equipamentosDaSala = given()
                .queryParam("salaId", salaId.longValue())
                .when().get("/equipamentos")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertFalse(equipamentosDaSala.isEmpty());
    }

    @Test
    void deveImportarResultadoDePlantaComSalaSemPosicoes() {
        Map<String, Object> body = Map.of(
                "resumo", "Imagem de sala sem posicoes rotuladas",
                "observacoes", "Importacao somente da sala",
                "sala", Map.of(
                        "nome", "Sala Sem Posicoes " + System.nanoTime(),
                        "descricao", "Sala com equipamento compartilhado",
                        "localizacao", "Bloco E",
                        "capacidade", 1,
                        "confianca", 0.8,
                        "equipamentos", List.of(
                                Map.of(
                                        "nome", "TV compartilhada",
                                        "tipo", "TV",
                                        "descricao", "TV na parede",
                                        "confianca", 0.82))),
                "posicoes", List.of());

        Number salaId = given()
                .contentType("application/json")
                .body(body)
                .when().post("/plantas/importar/resultado")
                .then()
                .statusCode(200)
                .body("salasCadastradas", is(1))
                .body("posicoesCadastradas", is(0))
                .body("equipamentosCadastrados", is(1))
                .body("sala.equipamentos[0].tipo", is("TV"))
                .extract().path("sala.id");

        given()
                .when().get("/salas/{id}", salaId.longValue())
                .then()
                .statusCode(200)
                .body("id", is(salaId.intValue()));
    }

    @Test
    void deveListarEquipamentosDentroDaSala() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long outraSalaId = criarSala(StatusRecurso.DISPONIVEL);
        Long equipamentoDaSalaId = criarEquipamento(salaId, StatusRecurso.DISPONIVEL);
        Long equipamentoDeOutraSalaId = criarEquipamento(outraSalaId, StatusRecurso.DISPONIVEL);

        List<Integer> ids = given()
                .queryParam("salaId", salaId)
                .when().get("/equipamentos")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertTrue(ids.contains(equipamentoDaSalaId.intValue()));
        assertFalse(ids.contains(equipamentoDeOutraSalaId.intValue()));
    }

    @Test
    void naoDeveAtualizarReservaCancelada() {
        Long salaId = criarSala(StatusRecurso.DISPONIVEL);
        Long reservaId = criarReserva(
                salaId,
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

        criarReserva(salaOcupadaId, inicio, fim);
        Long reservaId = criarReserva(salaOriginalId, inicio.plusHours(2), fim.plusHours(2));

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
                LocalDateTime.of(2026, 6, 12, 9, 0),
                LocalDateTime.of(2026, 6, 12, 10, 0));

        given()
                .when().delete("/salas/{id}", salaId)
                .then()
                .statusCode(409)
                .body("mensagem", is("Sala possui reservas vinculadas"));
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

    private Long criarEquipamento(Long salaId, StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "salaId", salaId,
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

    private Long criarUsuario(String nome, String role, String cargo) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", nome,
                        "role", role,
                        "cargo", cargo))
                .when().post("/usuarios")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarPosicao(StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "codigo", "P" + System.nanoTime(),
                        "descricao", "Mesa de trabalho",
                        "localizacao", "Bloco B",
                        "recursos", "Monitor e cadeira ergonomica",
                        "status", status.name()))
                .when().post("/posicoes")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarReserva(Long salaId, LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("salaId", salaId);
        body.put("responsavel", "Ana Costa");
        body.put("dataHoraInicio", inicio.toString());
        body.put("dataHoraFim", fim.toString());

        Number id = given()
                .contentType("application/json")
                .body(body)
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarReservaPosicao(Long posicaoId, LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("posicaoId", posicaoId);
        body.put("responsavel", "Ana Costa");
        body.put("dataHoraInicio", inicio.toString());
        body.put("dataHoraFim", fim.toString());

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
