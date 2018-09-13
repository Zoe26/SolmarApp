package com.idslatam.solmar.Models.Entities;

public class CargoFormFoto {
    public static final String TABLE_NAME = "CargoFormFoto";
    public static final String KEY_ID = "cargoFormFotoId";
    public static final String KEY_CODIGO_SINCRONIZACION = "codigoSincronizacion";
    public static final String KEY_TIPO_FOTO = "clienteCargaFotoId";
    public static final String KEY_INDICE = "indice";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_CREATED = "created";

    public long cargoFormFotoId;
    public String codigoSincronizacion;
    public String clienteCargaFotoId;
    public String indice;
    public String filePath;

    public CargoFormFoto(long cargoFormFotoId, String codigoSincronizacion, String clienteCargaFotoId, String indice, String filePath) {
        this.cargoFormFotoId = cargoFormFotoId;
        this.codigoSincronizacion = codigoSincronizacion;
        this.clienteCargaFotoId = clienteCargaFotoId;
        this.indice = indice;
        this.filePath = filePath;
    }

    public long getCargoFormFotoId() {
        return cargoFormFotoId;
    }

    public void setCargoFormFotoId(long cargoFormFotoId) {
        this.cargoFormFotoId = cargoFormFotoId;
    }
}


