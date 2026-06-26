package org.acme.agent.plant.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Locale;

import org.acme.agent.plant.dto.ImageBlockDTO;
import org.acme.agent.plant.dto.PlantAnalysisRequestDTO;
import org.acme.agent.plant.dto.ProcessedImageDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ImageProcessingService {

    private static final String DATA_IMAGE_PREFIX = "data:image/";

    @ConfigProperty(name = "ai.vision.max-image-bytes", defaultValue = "15728640")
    long maxImageBytes;

    public ProcessedImageDTO process(PlantAnalysisRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("Dados da imagem sao obrigatorios");
        }

        boolean hasUrl = hasText(request.getImageUrl());
        boolean hasBase64 = hasText(request.getBase64Image());

        if (!hasUrl && !hasBase64) {
            throw new BadRequestException("Informe imageUrl ou base64Image para analise visual");
        }

        if (hasUrl && hasBase64) {
            throw new BadRequestException("Informe apenas imageUrl ou base64Image, nao ambos");
        }

        return hasUrl ? processUrl(request.getImageUrl()) : processBase64(request.getBase64Image());
    }

    private ProcessedImageDTO processUrl(String imageUrl) {
        URI uri;
        try {
            uri = new URI(imageUrl.trim());
        } catch (URISyntaxException exception) {
            throw new BadRequestException("imageUrl invalida");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BadRequestException("imageUrl deve usar http ou https");
        }

        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        if (!path.isBlank() && path.contains(".") && !hasSupportedExtension(path)) {
            throw new BadRequestException("Formato da imagem nao suportado");
        }

        ProcessedImageDTO processed = new ProcessedImageDTO();
        processed.setImageReference(imageUrl.trim());
        processed.setBase64(false);
        processed.setMimeType(mimeTypeFromPath(path));
        processed.getBlocks().add(block(imageUrl.trim()));
        return processed;
    }

    private ProcessedImageDTO processBase64(String base64Image) {
        String value = base64Image.trim();
        String mimeType = "image/png";
        String rawBase64 = value;

        if (value.startsWith("data:")) {
            int commaIndex = value.indexOf(',');
            if (commaIndex < 0 || !value.startsWith(DATA_IMAGE_PREFIX)) {
                throw new BadRequestException("base64Image deve conter uma imagem valida");
            }
            mimeType = value.substring("data:".length(), commaIndex);
            rawBase64 = value.substring(commaIndex + 1);
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(rawBase64);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("base64Image invalida");
        }

        if (bytes.length == 0) {
            throw new BadRequestException("base64Image vazia");
        }

        if (bytes.length > maxImageBytes) {
            throw new WebApplicationException("Imagem grande demais para analise nesta etapa", 413);
        }

        String detectedMimeType = detectMimeType(bytes, mimeType);
        String dataUrl = "data:%s;base64,%s".formatted(detectedMimeType, rawBase64);

        ProcessedImageDTO processed = new ProcessedImageDTO();
        processed.setImageReference(dataUrl);
        processed.setMimeType(detectedMimeType);
        processed.setSizeBytes(bytes.length);
        processed.setBase64(true);
        processed.getBlocks().add(block(dataUrl));
        return processed;
    }

    private ImageBlockDTO block(String imageReference) {
        ImageBlockDTO block = new ImageBlockDTO();
        block.setImageReference(imageReference);
        block.setOffsetX(0);
        block.setOffsetY(0);
        return block;
    }

    private boolean hasSupportedExtension(String path) {
        return path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".webp")
                || path.endsWith(".gif");
    }

    private String mimeTypeFromPath(String path) {
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (path.endsWith(".webp")) {
            return "image/webp";
        }
        if (path.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/png";
    }

    private String detectMimeType(byte[] bytes, String declaredMimeType) {
        if (bytes.length >= 4
                && bytes[0] == (byte) 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3
                && bytes[0] == (byte) 0xFF
                && bytes[1] == (byte) 0xD8
                && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 12
                && bytes[0] == 0x52
                && bytes[1] == 0x49
                && bytes[2] == 0x46
                && bytes[3] == 0x46
                && bytes[8] == 0x57
                && bytes[9] == 0x45
                && bytes[10] == 0x42
                && bytes[11] == 0x50) {
            return "image/webp";
        }

        if (declaredMimeType != null && declaredMimeType.matches("image/(png|jpeg|jpg|webp|gif)")) {
            return "image/jpg".equals(declaredMimeType) ? "image/jpeg" : declaredMimeType;
        }

        throw new BadRequestException("Formato da imagem nao suportado");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
