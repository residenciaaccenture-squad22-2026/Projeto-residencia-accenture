package org.acme.agent.plant.service;

import org.acme.agent.plant.dto.PlantAnalysisRequestDTO;
import org.acme.agent.plant.dto.PlantAnalysisResponseDTO;
import org.acme.agent.plant.dto.ProcessedImageDTO;
import org.acme.agent.plant.prompt.PlantAnalysisPromptBuilder;
import org.acme.agent.plant.provider.AIProviderService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PlantAnalysisAgentService {

    @Inject
    ImageProcessingService imageProcessingService;

    @Inject
    PlantAnalysisPromptBuilder promptBuilder;

    @Inject
    AIProviderService aiProviderService;

    @Inject
    PlantAnalysisResultNormalizerService normalizerService;

    public PlantAnalysisResponseDTO analyze(PlantAnalysisRequestDTO request) {
        ProcessedImageDTO image = imageProcessingService.process(request);
        String prompt = promptBuilder.build(request);
        String rawJson = aiProviderService.analyzeImage(image, prompt);
        return normalizerService.normalize(rawJson, request.getRoomNameHint());
    }
}