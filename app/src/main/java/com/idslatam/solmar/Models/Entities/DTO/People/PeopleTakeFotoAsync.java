package com.idslatam.solmar.Models.Entities.DTO.People;

public class PeopleTakeFotoAsync {

    public String imageFilePath;
    public String tipoFoto;
    public Boolean success;

    public PeopleTakeFotoAsync(String imageFilePath, String tipoFoto) {
        this.imageFilePath = imageFilePath;
        this.tipoFoto = tipoFoto;
        this.success = false;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getTipoFoto() {
        return tipoFoto;
    }

    public void setTipoFoto(String tipoFoto) {
        this.tipoFoto = tipoFoto;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
