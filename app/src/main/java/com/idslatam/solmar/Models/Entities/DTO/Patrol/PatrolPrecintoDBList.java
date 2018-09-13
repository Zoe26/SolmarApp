package com.idslatam.solmar.Models.Entities.DTO.Patrol;

public class PatrolPrecintoDBList {

    public String filePath;
    public int indice;
    public String clienteMaterialFotoId;

    public PatrolPrecintoDBList(String filePath, int indice,String clienteMaterialFotoId) {
        this.filePath = filePath;
        this.indice = indice;
        this.clienteMaterialFotoId = clienteMaterialFotoId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public String getClienteMaterialFotoId() {
        return clienteMaterialFotoId;
    }

    public void setClienteMaterialFotoId(String clienteMaterialFotoId) {
        this.clienteMaterialFotoId = clienteMaterialFotoId;
    }
}
