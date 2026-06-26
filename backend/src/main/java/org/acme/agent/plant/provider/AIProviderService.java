package org.acme.agent.plant.provider;

import org.acme.agent.plant.dto.ProcessedImageDTO;

public interface AIProviderService {

    String analyzeImage(ProcessedImageDTO image, String prompt);
}