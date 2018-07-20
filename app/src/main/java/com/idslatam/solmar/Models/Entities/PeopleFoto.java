package com.idslatam.solmar.Models.Entities;

public class PeopleFoto {

    public static final String TABLE_NAME = "PeopleFoto";
    public static final String KEY_ID = "peopleFotoId";
    public static final String KEY_CODIGO_SINCRONIZACION = "codigoSincronizacion";
    public static final String KEY_TIPO_FOTO = "tipoFoto";
    public static final String KEY_INDICE = "indice";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_CREATED = "created";

    public long peopleFotoId;
    public String codigoSincronizacion;
    public int tipoFoto;
    public String indice;
    public String filePath;

    public PeopleFoto(long peopleFotoId, String codigoSincronizacion, int tipoFoto, String indice, String filePath) {
        this.peopleFotoId = peopleFotoId;
        this.codigoSincronizacion = codigoSincronizacion;
        this.tipoFoto = tipoFoto;
        this.indice = indice;
        this.filePath = filePath;
    }

    public long getPeopleFotoId() {
        return peopleFotoId;
    }

    public void setPeopleFotoId(long peopleFotoId) {
        this.peopleFotoId = peopleFotoId;
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

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
