package com.idslatam.solmar.Models.Entities.DTO.Patrol;

public class PatrolPrecintoDBList {

    public String filePath;
    public int indice;

    public PatrolPrecintoDBList(String filePath, int indice) {
        this.filePath = filePath;
        this.indice = indice;
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

}
