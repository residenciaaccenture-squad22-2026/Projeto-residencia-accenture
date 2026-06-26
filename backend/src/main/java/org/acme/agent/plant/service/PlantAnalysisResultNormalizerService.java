package org.acme.agent.plant.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.acme.agent.plant.dto.ApproximateCoordinateDTO;
import org.acme.agent.plant.dto.DetectedEquipmentDTO;
import org.acme.agent.plant.dto.DetectedPositionDTO;
import org.acme.agent.plant.dto.EquipmentSummaryDTO;
import org.acme.agent.plant.dto.PlantAnalysisResponseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PlantAnalysisResultNormalizerService {

    private static final Pattern POSITION_CODE_PATTERN = Pattern.compile("P\\d{3}");
    private static final String REVIEW_MESSAGE = "Recomenda-se revisao humana antes de salvar no banco.";

    @Inject
    ObjectMapper objectMapper;

    public PlantAnalysisResponseDTO normalize(String rawJson, String roomNameHint) {
        JsonNode root = parseJson(rawJson);
        validateRoot(root);

        List<DetectedPositionDTO> positions = normalizePositions(root.get("pos"));
        positions.sort(Comparator.comparingInt(DetectedPositionDTO::getLin)
                .thenComparingInt(DetectedPositionDTO::getCol)
                .thenComparing(position -> position.getCod() == null ? "" : position.getCod()));
        resequenceMissingOrInvalidCodes(positions);

        PlantAnalysisResponseDTO response = new PlantAnalysisResponseDTO();
        response.setSala(resolveRoomName(root.get("sala"), roomNameHint));
        response.setPos(positions);
        response.setTotalPos(positions.size());
        response.setResumoEq(buildSummary(positions));
        response.setConfGeral(resolveGeneralConfidence(positions, root.get("confGeral")));
        response.setRevisao(true);
        response.setObs(normalizeObservations(root.get("obs")));

        return response;
    }

    private JsonNode parseJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new WebApplicationException("Resposta da IA vazia", Response.Status.BAD_GATEWAY);
        }

        String candidate = rawJson.trim();
        try {
            return objectMapper.readTree(candidate);
        } catch (IOException firstException) {
            int start = candidate.indexOf('{');
            int end = candidate.lastIndexOf('}');
            if (start >= 0 && end > start) {
                try {
                    return objectMapper.readTree(candidate.substring(start, end + 1));
                } catch (IOException secondException) {
                    throw new WebApplicationException("JSON retornado pela IA e malformado", secondException,
                            Response.Status.BAD_GATEWAY);
                }
            }
            throw new WebApplicationException("JSON retornado pela IA e malformado", firstException,
                    Response.Status.BAD_GATEWAY);
        }
    }

    private void validateRoot(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new WebApplicationException("Resposta da IA deve ser um objeto JSON", Response.Status.BAD_GATEWAY);
        }
        JsonNode positions = root.get("pos");
        if (positions == null || !positions.isArray()) {
            throw new WebApplicationException("Resposta da IA deve conter pos como lista", Response.Status.BAD_GATEWAY);
        }
    }

    private List<DetectedPositionDTO> normalizePositions(JsonNode positionsNode) {
        List<DetectedPositionDTO> positions = new ArrayList<>();
        Map<String, DetectedPositionDTO> unique = new LinkedHashMap<>();
        int index = 1;

        for (JsonNode node : positionsNode) {
            if (node == null || !node.isObject()) {
                continue;
            }

            DetectedPositionDTO position = new DetectedPositionDTO();
            position.setCod(normalizePositionCode(text(node.get("cod")), index));
            position.setLin(positiveInt(node.get("lin"), index));
            position.setCol(positiveInt(node.get("col"), 1));
            position.setCoord(normalizeCoordinate(node.get("coord")));
            position.setEq(normalizeEquipment(node.get("eq")));
            position.setConf(PlantAnalysisDictionary.normalizeConfidence(text(node.get("conf"))));

            String key = uniqueKey(position);
            unique.putIfAbsent(key, position);
            index++;
        }

        positions.addAll(unique.values());
        return positions;
    }

    private ApproximateCoordinateDTO normalizeCoordinate(JsonNode coordNode) {
        if (coordNode == null || !coordNode.isObject()) {
            return new ApproximateCoordinateDTO(0, 0);
        }

        return new ApproximateCoordinateDTO(
                nonNegativeInt(coordNode.get("x"), 0),
                nonNegativeInt(coordNode.get("y"), 0));
    }

    private List<DetectedEquipmentDTO> normalizeEquipment(JsonNode equipmentNode) {
        List<DetectedEquipmentDTO> equipment = new ArrayList<>();
        if (equipmentNode == null || !equipmentNode.isArray()) {
            return equipment;
        }

        Map<String, DetectedEquipmentDTO> byType = new LinkedHashMap<>();
        for (JsonNode node : equipmentNode) {
            if (node == null || !node.isObject()) {
                continue;
            }

            String type = PlantAnalysisDictionary.normalizeEquipmentType(text(node.get("t")));
            int quantity = positiveInt(node.get("q"), 1);
            String confidence = PlantAnalysisDictionary.normalizeConfidence(text(node.get("c")));

            DetectedEquipmentDTO existing = byType.get(type);
            if (existing == null) {
                byType.put(type, new DetectedEquipmentDTO(type, quantity, confidence));
            } else {
                existing.setQ(existing.getQ() + quantity);
                existing.setC(moreConservative(existing.getC(), confidence));
            }
        }

        equipment.addAll(byType.values());
        equipment.sort(Comparator.comparing(DetectedEquipmentDTO::getT));
        return equipment;
    }

    private void resequenceMissingOrInvalidCodes(List<DetectedPositionDTO> positions) {
        int index = 1;
        for (DetectedPositionDTO position : positions) {
            if (position.getCod() == null || !POSITION_CODE_PATTERN.matcher(position.getCod()).matches()) {
                position.setCod(positionCode(index));
            }
            index++;
        }
    }

    private List<EquipmentSummaryDTO> buildSummary(List<DetectedPositionDTO> positions) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (DetectedPositionDTO position : positions) {
            if (position.getEq() == null) {
                continue;
            }
            for (DetectedEquipmentDTO equipment : position.getEq()) {
                totals.merge(equipment.getT(), equipment.getQ(), Integer::sum);
            }
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new EquipmentSummaryDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<String> normalizeObservations(JsonNode observationsNode) {
        List<String> observations = new ArrayList<>();
        if (observationsNode != null && observationsNode.isArray()) {
            for (JsonNode observation : observationsNode) {
                String value = text(observation);
                if (value != null && !value.isBlank()) {
                    observations.add(value.trim());
                }
            }
        }

        if (observations.stream().noneMatch(value -> REVIEW_MESSAGE.equalsIgnoreCase(value))) {
            observations.add(REVIEW_MESSAGE);
        }
        return observations;
    }

    private String resolveRoomName(JsonNode roomNode, String roomNameHint) {
        String roomName = text(roomNode);
        if (roomName != null && !roomName.isBlank()) {
            return roomName.trim();
        }
        if (roomNameHint != null && !roomNameHint.isBlank()) {
            return roomNameHint.trim();
        }
        return "Sala analisada";
    }

    private String resolveGeneralConfidence(List<DetectedPositionDTO> positions, JsonNode generalNode) {
        String confidence = PlantAnalysisDictionary.normalizeConfidence(text(generalNode));
        for (DetectedPositionDTO position : positions) {
            confidence = moreConservative(confidence, position.getConf());
            if (position.getEq() != null) {
                for (DetectedEquipmentDTO equipment : position.getEq()) {
                    confidence = moreConservative(confidence, equipment.getC());
                }
            }
        }
        return confidence;
    }

    private String normalizePositionCode(String value, int index) {
        if (value == null || value.isBlank()) {
            return positionCode(index);
        }

        String normalized = value.trim().toUpperCase().replace(" ", "");
        if (POSITION_CODE_PATTERN.matcher(normalized).matches()) {
            return normalized;
        }

        String digits = normalized.replaceAll("\\D", "");
        if (!digits.isBlank()) {
            return "P" + String.format("%03d", Integer.parseInt(digits));
        }

        return positionCode(index);
    }

    private String positionCode(int index) {
        return "P" + String.format("%03d", Math.max(index, 1));
    }

    private String uniqueKey(DetectedPositionDTO position) {
        ApproximateCoordinateDTO coord = position.getCoord();
        int bucketX = coord == null ? 0 : coord.getX() / 8;
        int bucketY = coord == null ? 0 : coord.getY() / 8;
        return position.getCod() + "|" + bucketX + "|" + bucketY;
    }

    private int positiveInt(JsonNode node, int fallback) {
        int value = node != null && node.canConvertToInt() ? node.asInt() : fallback;
        return Math.max(value, 1);
    }

    private int nonNegativeInt(JsonNode node, int fallback) {
        int value = node != null && node.canConvertToInt() ? node.asInt() : fallback;
        return Math.max(value, 0);
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isTextual() ? node.asText() : node.asText(null);
    }

    private String moreConservative(String current, String incoming) {
        int currentRank = confidenceRank(current);
        int incomingRank = confidenceRank(incoming);
        return incomingRank > currentRank ? incoming : current;
    }

    private int confidenceRank(String value) {
        return switch (PlantAnalysisDictionary.normalizeConfidence(value)) {
            case "A" -> 0;
            case "M" -> 1;
            default -> 2;
        };
    }
}