package org.acme;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;

import org.acme.agente.ChatbotReservaAgent;
import org.acme.agente.ReservaChatTools;
import org.acme.model.StatusRecurso;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ReservaChatToolsTest {

    @Inject
    ChatbotReservaAgent chatbotReservaAgent;

    @Inject
    ReservaChatTools reservaChatTools;

    @Test
    void deveRegistrarAgenteEFerramentasNoContexto() {
        assertNotNull(chatbotReservaAgent);
        assertNotNull(reservaChatTools);
    }

    @Test
    void deveBuscarUsuarioERecomendarPosicaoParaDesigner() {
        Long usuarioId = criarUsuario("Bianca Designer", "FUNCIONARIO", "Designer UX");
        criarPosicao("P-DESIGN-" + System.nanoTime(), "Mesa para design", "Monitor grande e mesa digitalizadora",
                StatusRecurso.DISPONIVEL);

        String usuario = reservaChatTools.buscarDadosUsuario(usuarioId);
        String recomendacao = reservaChatTools.recomendarPosicoes(usuarioId);

        assertTrue(usuario.contains("role=FUNCIONARIO"));
        assertTrue(recomendacao.contains("Para Designer"));
        assertTrue(recomendacao.contains("Monitor grande"));
    }

    @Test
    void deveReservarPosicaoEBloquearSegundaReservaAtivaParaFuncionario() {
        Long usuarioId = criarUsuario("Funcionario Agente", "FUNCIONARIO", "Dev");
        Long primeiraPosicaoId = criarPosicao("P-AGENTE-1-" + System.nanoTime(), "Mesa 1", "Monitor",
                StatusRecurso.DISPONIVEL);
        Long segundaPosicaoId = criarPosicao("P-AGENTE-2-" + System.nanoTime(), "Mesa 2", "Dock",
                StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 7, 1, 10, 0);

        String primeiraReserva = reservaChatTools.reservarPosicao(usuarioId, primeiraPosicaoId, inicio.toString(),
                fim.toString());
        String segundaReserva = reservaChatTools.reservarPosicao(usuarioId, segundaPosicaoId, inicio.plusHours(2).toString(),
                fim.plusHours(2).toString());

        assertTrue(primeiraReserva.contains("confirmada com sucesso"));
        assertTrue(segundaReserva.contains("voce ja possui uma reserva ativa"));
    }

    @Test
    void devePermitirAdminCancelarReservaDeOutroUsuario() {
        Long funcionarioId = criarUsuario("Dono Reserva", "FUNCIONARIO", "Analista");
        Long adminId = criarUsuario("Admin Agente", "ADMIN", "Facilities");
        Long posicaoId = criarPosicao("P-CANCEL-" + System.nanoTime(), "Mesa cancelamento", "Monitor",
                StatusRecurso.DISPONIVEL);
        LocalDateTime inicio = LocalDateTime.of(2026, 7, 2, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 7, 2, 10, 0);

        Number reservaId = given()
                .contentType("application/json")
                .body(Map.of(
                        "posicaoId", posicaoId,
                        "usuarioId", funcionarioId,
                        "responsavel", "Dono Reserva",
                        "dataHoraInicio", inicio.toString(),
                        "dataHoraFim", fim.toString()))
                .when().post("/reservas")
                .then()
                .statusCode(201)
                .extract().path("id");

        String resposta = reservaChatTools.cancelarReserva(adminId, reservaId.longValue());

        assertTrue(resposta.contains("cancelada com sucesso"));
    }

    @Test
    void devePermitirAdminDesativarSala() {
        Long adminId = criarUsuario("Admin Manutencao", "ADMIN", "Facilities");
        Long salaId = criarSala("Sala Agente " + System.nanoTime(), StatusRecurso.DISPONIVEL);

        String resposta = reservaChatTools.desativarRecurso(adminId, "SALA", salaId);
        String salas = reservaChatTools.listarSalas();

        assertTrue(resposta.contains("colocada em manutencao"));
        assertTrue(salas.contains("id=" + salaId));
        assertTrue(salas.contains("status=MANUTENCAO"));
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

    private Long criarSala(String nome, StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "nome", nome,
                        "capacidade", 8,
                        "localizacao", "Bloco Agente",
                        "status", status.name()))
                .when().post("/salas")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }

    private Long criarPosicao(String codigo, String descricao, String recursos, StatusRecurso status) {
        Number id = given()
                .contentType("application/json")
                .body(Map.of(
                        "codigo", codigo,
                        "descricao", descricao,
                        "localizacao", "Bloco Agente",
                        "recursos", recursos,
                        "status", status.name()))
                .when().post("/posicoes")
                .then()
                .statusCode(201)
                .extract().path("id");

        return id.longValue();
    }
}
