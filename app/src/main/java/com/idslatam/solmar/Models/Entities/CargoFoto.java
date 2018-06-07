package com.idslatam.solmar.Models.Entities;

public class CargoFoto {

    public static final String TABLE_NAME = "CargoFoto";
    public static final String KEY_ID = "cargoFotoId";
    public static final String KEY_CODIGO_SINCRONIZACION = "codigoSincronizacion";
    public static final String KEY_TIPO_FOTO = "tipoFoto";
    public static final String KEY_INDICE = "indice";
    public static final String KEY_FILE_PATH = "filePath";

    public long cargoFotoId;
    public String codigoSincronizacion;
    public int tipoFoto;
    public String indice;
    public String filePath;

    public CargoFoto(long cargoFotoId, String codigoSincronizacion, int tipoFoto, String filePath,String indice) {
        this.cargoFotoId = cargoFotoId;
        this.codigoSincronizacion = codigoSincronizacion;
        this.tipoFoto = tipoFoto;
        this.filePath = filePath;
        this.indice = indice;
    }

    public long getCargoFotoId() {
        return cargoFotoId;
    }

    public void setCargoFotoId(long cargoFotoId) {
        this.cargoFotoId = cargoFotoId;
    }

    public String getCodigoSincronizacion() {
        return codigoSincronizacion;
    }

    public void setCodigoSincronizacion(String codigoSincronizacion) {
        this.codigoSincronizacion = codigoSincronizacion;
    }

    public int getTipoFoto() {
        return tipoFoto;
    }

    public void setTipoFoto(int tipoFoto) {
        this.tipoFoto = tipoFoto;
    }

    public String getIndex() {
        return indice;
    }

    public void setIndex(String indice) {
        this.indice = indice;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
