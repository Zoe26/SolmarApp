package com.idslatam.solmar.Models.Entities.DTO.Cargo;

public class CargoFormTakeFotoAsync {
    public int tipoFoto;
    public String imageFilePath;
    public String clienteCargaFotoId;
    public String index;
    public Boolean success;

    public CargoFormTakeFotoAsync(int tipoFoto, String imageFilePath, String clienteCargaFotoId, String index) {
        this.tipoFoto = tipoFoto;
        this.imageFilePath = imageFilePath;
        this.clienteCargaFotoId = clienteCargaFotoId;
        this.index = index;
        this.success = false;
    }

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

    public String getClienteCargaFotoId() {
        return clienteCargaFotoId;
    }

    public void setClienteCargaFotoId(String clienteCargaFotoId) {
        this.clienteCargaFotoId = clienteCargaFotoId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}


