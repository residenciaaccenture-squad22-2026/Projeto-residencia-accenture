package org.acme.controller;

import org.acme.agente.ChatbotReservaAgent;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
        return chatbot.conversar(request.usuarioId, request.mensagem);
    }
}
