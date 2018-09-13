package com.idslatam.solmar.Models.Entities;

/**
 * Created by desarrollo03 on 7/19/17.
 */

public class PatrolPrecinto {

    public static final String TABLE_PATROL_PRECINTO = "PatrolPrecinto";

    // Labels Table Columns names
    public static final String KEY_ID_PatrolPrecinto = "PatrolPrecintoId";
    public static final String KEY_indice = "Indice";
    public static final String KEY_Foto = "Foto";
    public static final String KEY_ClienteMaterialFotoId = "ClienteMaterialFotoId";


    public long PatrolPrecintoId;
    public String Indice;
    public String Foto;
    public String ClienteMaterialFotoId;

}
