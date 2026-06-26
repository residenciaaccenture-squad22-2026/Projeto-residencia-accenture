package org.acme.agent.plant.resource;

import org.acme.agent.plant.dto.PlantAnalysisRequestDTO;
import org.acme.agent.plant.dto.PlantAnalysisResponseDTO;
import org.acme.agent.plant.service.PlantAnalysisAgentService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/agents/plant-analysis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlantAnalysisAgentResource {

    @Inject
    PlantAnalysisAgentService plantAnalysisAgentService;

    @POST
    public PlantAnalysisResponseDTO analyze(@Valid PlantAnalysisRequestDTO request) {
        return plantAnalysisAgentService.analyze(request);
    }
}