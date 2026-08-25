package com.clarimire.entity;

import lombok.Data;
import java.util.List;

@Data
public class ImportResult {
    private int importedData;
    private int skippedData;
    private List<String> skippedReservoirs;
    private String message;
    private boolean success;

    public ImportResult() {}

    public ImportResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ImportResult(int importedData, int skippedData, List<String> skippedReservoirs, String message) {
        this.importedData = importedData;
        this.skippedData = skippedData;
        this.skippedReservoirs = skippedReservoirs;
        this.message = message;
        this.success = true;
    }
}
