package org.acme.controller;

import org.acme.dto.PlantaAnaliseResponse;
import org.acme.dto.PlantaImagemRequest;
import org.acme.dto.PlantaImportacaoResponse;
import org.acme.service.PlantaImportacaoService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/plantas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlantaController {

    @Inject
    PlantaImportacaoService plantaImportacaoService;

    @POST
    @Path("/analisar")
    public PlantaAnaliseResponse analisarPlanta(@Valid PlantaImagemRequest request) {
        return plantaImportacaoService.analisar(request);
    }

    @POST
    @Path("/importar")
    public PlantaImportacaoResponse importarPlanta(@Valid PlantaImagemRequest request) {
        return plantaImportacaoService.analisarEImportar(request);
    }

    @POST
    @Path("/importar/resultado")
    public PlantaImportacaoResponse importarResultado(PlantaAnaliseResponse analise) {
        return plantaImportacaoService.importarResultado(analise);
    }
}
