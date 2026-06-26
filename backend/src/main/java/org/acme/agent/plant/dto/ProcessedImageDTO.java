package org.acme.agent.plant.dto;

import java.util.ArrayList;
import java.util.List;

public class ProcessedImageDTO {

    private String imageReference;
    private String mimeType;
    private long sizeBytes;
    private boolean base64;
    private List<ImageBlockDTO> blocks = new ArrayList<>();

    public String getImageReference() {
        return imageReference;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(boolean base64) {
        this.base64 = base64;
    }

    public List<ImageBlockDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ImageBlockDTO> blocks) {
        this.blocks = blocks;
    }
}