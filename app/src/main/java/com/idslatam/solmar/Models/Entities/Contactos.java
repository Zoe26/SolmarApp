package com.idslatam.solmar.Models.Entities;

/**
 * Created by desarrollo03 on 3/19/17.
 */

public class Contactos {

    public static final String TABLE_CONTACTOS = "Contactos";

    // Labels Table Columns names
    public static final String KEY_ID_CONTACTOS = "ContactosId";
    public static final String KEY_Nombre = "Nombre";
    public static final String KEY_PrimerNumero = "PrimerNumero";
    public static final String KEY_SegundoNumero = "SegundoNumero";

    public int ContactosId;
    public String Nombre;
    public int PrimerNumero;
    public int SegundoNumero;

}
