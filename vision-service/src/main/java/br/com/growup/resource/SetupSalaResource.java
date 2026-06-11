package br.com.growup.resource;

import br.com.growup.dto.AnaliseHierarquicaDTO;
import br.com.growup.dto.ProcessarSalaRequest;
import br.com.growup.service.AiVisionService;
import br.com.growup.service.SupabaseService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/setup-sala")
public class SetupSalaResource {

    @Inject
    AiVisionService visionService;

    @Inject
    SupabaseService supabaseService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response orquestrarCadastroDaSala(ProcessarSalaRequest request) {
        
        AnaliseHierarquicaDTO dadosIa = visionService.mapearPlantaHierarquica(request.imageUrl());

        if (dadosIa.mesas() == null || dadosIa.mesas().isEmpty()) {
            return Response.status(400).entity(Map.of("erro", "Nenhuma estrutura detectada.")).build();
        }

        String nomeSala = request.nomeSala() != null ? request.nomeSala() : dadosIa.nomeSugeridoSala();
        Long salaId = supabaseService.criarSala(nomeSala, request.localizacao());

        int totalPosicoes = 0;

        for (AnaliseHierarquicaDTO.MesaIa mesa : dadosIa.mesas()) {
            Long mesaId = supabaseService.criarMesa(salaId, mesa.codigoMesa());

            if (mesa.posicoes() != null) {
                for (AnaliseHierarquicaDTO.PosicaoIa cadeira : mesa.posicoes()) {
                    Long posicaoId = supabaseService.criarPosicao(mesaId, cadeira.codigoCadeira(), cadeira.coordenadasX(), cadeira.coordenadasY());
                    totalPosicoes++;

                    if (cadeira.recursos() != null) {
                        for (AnaliseHierarquicaDTO.RecursoIa recurso : cadeira.recursos()) {
                            supabaseService.criarRecurso(posicaoId, recurso.nomeModelo(), recurso.categoria());
                        }
                    }
                }
            }
        }

        return Response.ok(Map.of(
            "status", "SUCESSO",
            "mensagem", "Infraestrutura da sala gerada e salva no banco!",
            "sala_id", salaId,
            "total_mesas", dadosIa.mesas().size(),
            "total_posicoes", totalPosicoes
        )).build();
    }
}