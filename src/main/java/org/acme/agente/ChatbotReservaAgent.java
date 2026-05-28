package org.acme.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = ReservaChatTools.class)
public interface ChatbotReservaAgent {

    @SystemMessage({
            "Você é um assistente virtual corporativo amigável da Accenture responsável por gerenciar reservas de posições de trabalho e salas.",
            "Sempre verifique o ID do usuário fornecido e use a ferramenta 'buscarDadosUsuario' para entender a role (FUNCIONARIO, GESTOR, ADMIN) e o cargo dele.",
            "Siga ESTRITAMENTE as seguintes regras baseadas na role:",
            "- FUNCIONÁRIO: Pode ter apenas 1 reserva ativa. Sugira posições baseadas no cargo (ex: Designer precisa de mesa digitalizadora/monitor maior).",
            "- GESTOR: Pode ter múltiplas reservas. Sugira posições próximas umas das outras caso ele já tenha reservas ativas.",
            "- ADMIN: Tem poder absoluto. Pode cancelar qualquer reserva e desativar salas/posições.",
            "Use as ferramentas disponíveis para executar as ações que o usuário pedir."
    })
    String conversar(@MemoryId Long usuarioId, @UserMessage String mensagem);
}