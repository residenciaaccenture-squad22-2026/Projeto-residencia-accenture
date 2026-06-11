// package br.com.growup.resource;

// import br.com.growup.dto.AlocacaoRequest;
// import br.com.growup.dto.AnaliseResponse;
// import br.com.growup.dto.ElementoEspacial;
// import br.com.growup.dto.db.FuncionarioDTO;
// import br.com.growup.service.AiVisionService;
// import br.com.growup.service.SupabaseService;
// import jakarta.inject.Inject;
// import jakarta.ws.rs.Consumes;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.Path;
// import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;
// import java.util.Map;

// @Path("/api/alocacao")
// public class AlocacaoResource {

//     @Inject
//     AiVisionService visionService;

//     @Inject
//     SupabaseService supabaseService;

//     @POST
//     @Consumes(MediaType.APPLICATION_JSON)
//     @Produces(MediaType.APPLICATION_JSON)
//     public Response alocarFuncionario(AlocacaoRequest request) {
        
//         // 1. Busca os dados reais utilizando o DTO mapeado
//         FuncionarioDTO funcionario = supabaseService.buscarFuncionarioPorId(request.funcionarioId());
        
//         if (funcionario == null) {
//             return Response.status(Response.Status.NOT_FOUND)
//                     .entity(Map.of("erro", "Funcionário não encontrado no banco."))
//                     .build();
//         }

//         String preferenciaBanco = funcionario.posicaoPreferencial() != null ? 
//                                   funcionario.posicaoPreferencial() : "Sem preferência";

//         // 2. IA mapeia a imagem e retorna X/Y
//         AnaliseResponse mapaPlanta = visionService.mapearPlantaBaixa(request.imageUrl());

//         // 3. Algoritmo calcula o melhor assento
//         ElementoEspacial assentoIdeal = visionService.calcularMelhorAssento(mapaPlanta, preferenciaBanco);

//         // Retorno final pronto
//         return Response.ok(Map.of(
//             "funcionario", funcionario.nome(),
//             "cargo", funcionario.cargo(),
//             "preferencia", preferenciaBanco,
//             "assentoAlocado", assentoIdeal,
//             "coordenadas", Map.of("x", assentoIdeal.x(), "y", assentoIdeal.y())
//         )).build();
//     }
// }