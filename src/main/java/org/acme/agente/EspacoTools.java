package org.acme.agent;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.service.ReservaService;

import java.util.List;

@ApplicationScoped
public class EspacoTools {

    @Inject
    ReservaService reservaService;

    @Tool("Busca uma lista de salas e posições de trabalho que estão ativas e disponíveis no banco de dados.")
    public List<Espaco> buscarEspacosAtivos() {
        return reservaService.buscarEspacosDisponiveis();
    }
}