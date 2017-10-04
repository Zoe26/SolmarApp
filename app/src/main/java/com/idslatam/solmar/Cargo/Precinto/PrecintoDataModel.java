package com.idslatam.solmar.Cargo.Precinto;

/**
 * Created by desarrollo03 on 5/22/17.
 */

public class PrecintoDataModel {

    String num;
    String foto;

    public PrecintoDataModel(String num, String foto) {
        this.num=num;
        this.foto=foto;
    }

    public String getNum() {
        return num;
    }

    public String getFoto() {
        return foto;
    }

}
