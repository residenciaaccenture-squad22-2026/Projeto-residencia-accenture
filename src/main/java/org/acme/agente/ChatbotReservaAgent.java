package org.acme.agente;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V; // IMPORTANTE: Este import garante que o @V funcione
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = ReservaChatTools.class)
public interface ChatbotReservaAgent {

    @SystemMessage({
            "Você é um assistente virtual de reservas da Accenture.",
            "Para listar salas e posições de trabalho, use a ferramenta de buscar espaços disponíveis.",
            "Para mostrar as reservas do usuário, use a ferramenta de buscarMinhasReservas passando o ID dele.",
            "Você tem acesso ao ID do usuário atual em memória. Use esse ID ao chamar as ferramentas de criação ou cancelamento.",
            "Nunca invente informações. Leia do banco de dados."
    })
    @UserMessage("Meu ID de usuário no sistema é: {id_usuario}\n\nO que eu preciso: {mensagem}")
    String conversar(
            @MemoryId Long memoryId,
            @V("id_usuario") Long idUsuario,
            @V("mensagem") String mensagem
    );
}