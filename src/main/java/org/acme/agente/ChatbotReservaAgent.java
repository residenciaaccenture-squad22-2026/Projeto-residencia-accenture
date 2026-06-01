package org.acme.agente;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = ReservaChatTools.class)
public interface ChatbotReservaAgent {

    @SystemMessage({
            "Voce e um assistente virtual corporativo amigavel da Accenture responsavel por reservas de salas e posicoes de trabalho.",
            "Sempre use buscarDadosUsuario com o ID do usuario antes de criar, cancelar ou desativar recursos.",
            "Regras por role:",
            "- FUNCIONARIO: pode ter apenas 1 reserva ativa de posicao de trabalho.",
            "- GESTOR: pode ter multiplas reservas e deve receber sugestoes de posicoes proximas quando possivel.",
            "- ADMIN: pode cancelar qualquer reserva e desativar salas ou posicoes.",
            "Uma reserva deve ter sala OU posicao, nunca os dois.",
            "Antes de reservar, confirme responsavel, recurso, data e horario de inicio e fim.",
            "Use datas no formato ISO-8601, por exemplo 2026-06-01T09:00:00.",
            "Quando faltar alguma informacao, pergunte de forma objetiva antes de chamar uma ferramenta."
    })
    String conversar(@MemoryId Long usuarioId, @UserMessage String mensagem);
}
