package com.amazonaws.kinesisvideo.demoapp.rest.model;

import com.google.gson.annotations.SerializedName;

public class FaceDetectionResult {

    @SerializedName("isFace")
    private boolean isFace;

    @SerializedName("isSmile")
    private boolean isSmile;

    public FaceDetectionResult(boolean isFace, boolean isSmile) {
        this.isFace = isFace;
        this.isSmile = isSmile;
    }

    public boolean isFace() {
        return isFace;
    }

    public boolean isSmile() {
        return isSmile;
    }

    @Override
    public String toString() {
        return "isFace: " + isFace + " isSmile: " + isSmile;
    }
}
