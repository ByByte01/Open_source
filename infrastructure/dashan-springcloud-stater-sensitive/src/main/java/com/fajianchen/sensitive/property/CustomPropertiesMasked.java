package com.fajianchen.sensitive.property;

public class CustomPropertiesMasked {
    private String masked;
    private int startMask;
    private int endMask;

    public String getMasked() {
        return masked;
    }

    public void setMasked(String masked) {
        this.masked = masked;
    }

    public int getStartMask() {
        return startMask;
    }

    public void setStartMask(int startMask) {
        this.startMask = startMask;
    }

    public int getEndMask() {
        return endMask;
    }

    public void setEndMask(int endMask) {
        this.endMask = endMask;
    }
}
