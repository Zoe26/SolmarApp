package com.idslatam.solmar.Models.Entities;

/**
 * Created by desarrollo03 on 6/4/17.
 */

public class Cargo {

    public static final String TABLE_CARGO = "Cargo";

    // Labels Table Columns names
    public static final String KEY_ID_Cargo = "CargoId";
    public static final String KEY_Initial = "Initial";
    public static final String KEY_Placa = "Placa";
    public static final String KEY_NombrePlaca = "NombrePlaca";
    public static final String KEY_TipoCarga = "TipoCarga";
    public static final String KEY_EppCasco = "EppCasco";
    public static final String KEY_EppChaleco = "EppChaleco";
    public static final String KEY_EppBotas = "EppBotas";
    public static final String KEY_Dni = "Dni";
    public static final String KEY_isLicencia = "isLicencia";
    public static final String KEY_NroOR = "NroOR";
    public static final String KEY_CantidadBultos = "CantidadBultos";
    public static final String KEY_isCarga = "isCarga";
    public static final String KEY_isIngreso = "isIngreso";
    public static final String KEY_fotoDelantera = "fotoDelantera";
    public static final String KEY_fotoTracera = "fotoTracera";
    public static final String KEY_fotoPanoramica = "fotoPanoramica";

    public static final String KEY_tamanoContenedor = "tamanoContenedor"; //iniciar
    public static final String KEY_codigoContenedor = "codigoContenedor";
    public static final String KEY_numeroPrecintos = "numeroPrecintos";
    public static final String KEY_origenDestino = "origenDestino";
    public static final String KEY_tipoDocumento = "tipoDocumento";//iniciar
    public static final String KEY_numeroDocumento = "numeroDocumento";
    public static final String KEY_json= "json";
    public static final String KEY_GuiaRemision = "GuiaRemision";



    public int CargoId;
    public String Initial;
    public String TipoCarga;
    public String isLicencia;
    public String isCarga;
    public String EppCasco;
    public String EppChaleco;
    public String EppBotas;
    public String tamanoContenedor;
    public String tipoDocumento;


}
