package com.idslatam.solmar.Models.Entities.DTO.Patrol;

public class PatrolTakeFotoAsync {

    public String imageFilePath;
    public String indexPrecinto;
    public Boolean success;

    public PatrolTakeFotoAsync(String imageFilePath, String indexPrecinto) {
        this.imageFilePath = imageFilePath;
        this.indexPrecinto = indexPrecinto;
        this.success = false;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getIndexPrecinto() {
        return indexPrecinto;
    }

    public void setIndexPrecinto(String indexPrecinto) {
        this.indexPrecinto = indexPrecinto;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
