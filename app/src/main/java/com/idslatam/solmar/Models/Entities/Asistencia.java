package com.idslatam.solmar.Models.Entities;

/**
 * Created by Luis on 26/10/2016.
 */

public class Asistencia {

    public static final String TABLE_ASISTENCIA = "Asistencia";

    // Labels Table Columns names
    public static final String KEY_ID_Asistencia = "AsistenciaId";
    public static final String KEY_Numero = "NumeroAs";
    public static final String KEY_Asistencia = "Asistencia";
    public static final String KEY_DispositivoId = "DispositivoId";
    public static final String KEY_FechaInicio = "FechaInicio";
    public static final String KEY_FechaTermino = "FechaTermino";
    public static final String KEY_Fotocheck = "Fotocheck";


    public int AsistenciaId;
    public String NumeroAs;
    public String Asistencia;
    public String DispositivoId;
    public String FechaInicio;
    public String FechaTermino;
    public String Fotocheck;
}
