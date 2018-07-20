package com.idslatam.solmar.Models.Entities;

public class PatrolFoto {
    public static final String TABLE_NAME = "PatrolFoto";
    public static final String KEY_ID = "patrolFotoId";
    public static final String KEY_CODIGO_SINCRONIZACION = "codigoSincronizacion";
    public static final String KEY_INDICE = "indice";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_CREATED = "created";

    public long patrolFotoId;
    public String codigoSincronizacion;
    public String indice;
    public String filePath;

    public PatrolFoto(long patrolFotoId, String codigoSincronizacion, String indice, String filePath) {
        this.patrolFotoId = patrolFotoId;
        this.codigoSincronizacion = codigoSincronizacion;
        this.indice = indice;
        this.filePath = filePath;
    }

    public long getPatrolFotoId() {
        return patrolFotoId;
    }

    public void setPatrolFotoId(long patrolFotoId) {
        this.patrolFotoId = patrolFotoId;
    }

    public String getCodigoSincronizacion() {
        return codigoSincronizacion;
    }

    public void setCodigoSincronizacion(String codigoSincronizacion) {
        this.codigoSincronizacion = codigoSincronizacion;
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
