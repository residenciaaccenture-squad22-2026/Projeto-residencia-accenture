package org.acme.agent.plant.service;

import java.util.Map;
import java.util.Set;

public final class PlantAnalysisDictionary {

    public static final Set<String> EQUIPMENT_TYPES = Set.of(
            "MON", "TEC", "MOU", "CPU", "NOT", "CAD", "MES", "PRO", "TOM", "OUT");

    public static final Set<String> CONFIDENCE_LEVELS = Set.of("A", "M", "B");

    public static final Map<String, String> EQUIPMENT_LABELS = Map.ofEntries(
            Map.entry("MON", "Monitor"),
            Map.entry("TEC", "Teclado"),
            Map.entry("MOU", "Mouse"),
            Map.entry("CPU", "Computador"),
            Map.entry("NOT", "Notebook"),
            Map.entry("CAD", "Cadeira"),
            Map.entry("MES", "Mesa"),
            Map.entry("PRO", "Projetor"),
            Map.entry("TOM", "Tomada"),
            Map.entry("OUT", "Outro"));

    private PlantAnalysisDictionary() {
    }

    public static String normalizeEquipmentType(String value) {
        if (value == null || value.isBlank()) {
            return "OUT";
        }

        String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "MON", "MONITOR", "TELA" -> "MON";
            case "TEC", "TECLADO", "KEYBOARD" -> "TEC";
            case "MOU", "MOUSE" -> "MOU";
            case "CPU", "COMPUTADOR", "PC", "DESKTOP" -> "CPU";
            case "NOT", "NOTEBOOK", "LAPTOP", "NB" -> "NOT";
            case "CAD", "CADEIRA", "CHAIR" -> "CAD";
            case "MES", "MESA", "TABLE" -> "MES";
            case "PRO", "PROJETOR", "PROJECTOR" -> "PRO";
            case "TOM", "TOMADA", "OUTLET", "POWER" -> "TOM";
            default -> EQUIPMENT_TYPES.contains(normalized) ? normalized : "OUT";
        };
    }

    public static String normalizeConfidence(String value) {
        if (value == null || value.isBlank()) {
            return "B";
        }

        String normalized = value.trim().toUpperCase();
        return CONFIDENCE_LEVELS.contains(normalized) ? normalized : "B";
    }
}