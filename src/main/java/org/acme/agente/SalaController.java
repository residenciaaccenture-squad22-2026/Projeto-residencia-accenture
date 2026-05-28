package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.ai.ChatbotReservaAgent;

@Path("/api/chat")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    @Inject
    ChatbotReservaAgent chatbot;

    public static class MensagemRequest {
        public Long usuarioId;
        public String mensagem;
    }

    @POST
    public String interagirComBot(MensagemRequest request) {
        // O LangChain4j se encarregará de interpretar o texto,
        // decidir quais funções do @Tool chamar (ex: consultar cargo, fazer reserva)
        // e devolver a resposta humanizada.
        return chatbot.conversar(request.usuarioId, request.mensagem);
    }
}