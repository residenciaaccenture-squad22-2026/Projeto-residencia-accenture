package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.agente.ChatbotReservaAgent;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON) // 1. Mudamos para retornar JSON
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    @Inject
    ChatbotReservaAgent chatbot;

    // DTO de Entrada (Request)
    public static class MensagemRequest {
        public Long usuarioId;
        public String cargo;
        public String role;
        public String mensagem;
    }

    // 2. Novo DTO de Saída (Response) para formatar o JSON
    public static class MensagemResponse {
        public String resposta;

        public MensagemResponse(String resposta) {
            this.resposta = resposta;
        }
    }

    @POST
    // 3. Alteramos o retorno do método para a nossa nova classe
    public MensagemResponse interagirComBot(MensagemRequest request) {
        // O LangChain4j processa a IA e devolve o texto
        String respostaDaIA = chatbot.conversar(request.usuarioId, request.usuarioId, request.mensagem);

        // Empacotamos o texto no objeto que será convertido em JSON
        return new MensagemResponse(respostaDaIA);
    }
}