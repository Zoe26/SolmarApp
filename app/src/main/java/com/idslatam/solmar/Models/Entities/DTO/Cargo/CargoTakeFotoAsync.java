package com.idslatam.solmar.Models.Entities.DTO.Cargo;

public class CargoTakeFotoAsync {
    public CargoTakeFotoAsync(int tipoFoto,String imageFilePath,String imageReducedFilePath, String indexPrecinto){
        this.tipoFoto = tipoFoto;
        this.imageFilePath = imageFilePath;
        this.imageReducedFilePath = imageReducedFilePath;
        this.indexPrecinto = indexPrecinto;
        this.success = false;
    }
    public int tipoFoto;
    public String imageFilePath;
    public String imageReducedFilePath;
    public String indexPrecinto;
    public Boolean success;

    public int getTipoFoto() {
        return tipoFoto;
    }

    public void setTipoFoto(int tipoFoto) {
        this.tipoFoto = tipoFoto;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getImageReducedFilePath() {
        return imageReducedFilePath;
    }

    public void setImageReducedFilePath(String imageReducedFilePath) {
        this.imageReducedFilePath = imageReducedFilePath;
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
