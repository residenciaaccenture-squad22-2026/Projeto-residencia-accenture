package org.acme.controller;

import java.util.List;

import org.acme.dto.StatusApiResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusController {

    @GET
    public StatusApiResponse status() {
        return new StatusApiResponse(
                "residencia-accenture-api",
                "online",
                List.of("/salas", "/equipamentos", "/reservas", "/disponibilidade", "/q/health", "/swagger-ui"));
    }
}
