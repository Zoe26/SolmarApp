package com.idslatam.solmar.Patrol.Contenedor;

/**
 * Created by desarrollo03 on 5/22/17.
 */

public class DataModel {

    String name;
    String uri;
    String ClienteMaterialFotoId;

    public DataModel(String name, String uri,String clienteMaterialFotoId) {
        this.name=name;
        this.uri=uri;
        this.ClienteMaterialFotoId = clienteMaterialFotoId;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getClienteMaterialFotoId() {
        return ClienteMaterialFotoId;
    }
}
