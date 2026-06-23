package br.com.growup.resource;

import br.com.growup.dto.ProcessarSalaRequest;
import br.com.growup.dto.SetupConfirmadoRequest;
import br.com.growup.dto.AnaliseHierarquicaDTO;
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
    AiVisionService aiVisionService;

    @Inject
    SupabaseService supabaseService;

    // ETAPA 1: Apenas analisa a foto e devolve o rascunho
    @POST
    @Path("/analisar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response gerarRascunho(ProcessarSalaRequest request) {
        try {
            AnaliseHierarquicaDTO rascunho = aiVisionService.mapearPlantaHierarquica(request.imageUrl());
            return Response.ok(rascunho).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("erro", e.getMessage())).build();
        }
    }

    // ETAPA 2: Recebe o rascunho revisado e salva no banco
    @POST
    @Path("/confirmar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmarESalvar(SetupConfirmadoRequest request) {
        try {
            // 1. Cria a Sala
            Long salaId = supabaseService.criarSala(request.nome(), request.localizacao());

            // 2. Cria as Mesas
            for (AnaliseHierarquicaDTO.MesaIa mesa : request.analiseRevisada().mesas()) {
                Long mesaId = supabaseService.criarMesa(salaId, mesa.codigoMesa());

                // 3. Cria as Posições (Cadeiras)
                for (AnaliseHierarquicaDTO.PosicaoIa cadeira : mesa.posicoes()) {
                    Long posicaoId = supabaseService.criarPosicao(
                                                                    mesaId, 
                                                                    cadeira.codigoCadeira(), 
                                                                    cadeira.coordenadasX(), 
                                                                    cadeira.coordenadasY()
                                                                );
                    // 4. Cria os Recursos
                    if (cadeira.recursos() != null) {
                        for (AnaliseHierarquicaDTO.RecursoIa recurso : cadeira.recursos()) {
                            supabaseService.criarRecurso(posicaoId, recurso.nomeModelo(), recurso.categoria());
                        }
                    }
                }
            }
            return Response.ok(Map.of("status", "Setup salvo com sucesso!")).build();
            
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("erro", e.getMessage())).build();
        }
    }
}