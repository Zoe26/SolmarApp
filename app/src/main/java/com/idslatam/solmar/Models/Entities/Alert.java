package com.idslatam.solmar.Models.Entities;

/**
 * Created by Luis on 26/10/2016.
 */

public class Alert {
    public static final String TABLE_ALERT = "Alert";

    // Labels Table Columns names
    public static final String KEY_ID_ALERT = "AlertId";
    public static final String KEY_NumeroA = "NumeroCel";
    public static final String KEY_FechaMarcacion = "FechaMarcacion";
    public static final String KEY_FechaEsperada = "FechaEsperada";
    public static final String KEY_FechaProxima = "FechaProxima";
    public static final String KEY_FlagTiempo = "FlagTiempo";
    public static final String KEY_MargenAceptado = "MargenAceptado";
    public static final String KEY_LatitudA = "Latitud";
    public static final String KEY_LongitudA = "Longitud";
    public static final String KEY_EstadoA = "Estado";
    public static final String KEY_EstadoBoton = "EstadoBoton";
    public static final String KEY_FechaEsperadaIso = "FechaEsperadaIso";
    public static final String KEY_FechaEsperadaIsoFin = "FechaEsperadaIsoFin";
    public static final String KEY_DispositivoId = "DispositivoId";
    public static final String KEY_CodigoEmpleado = "CodigoEmpleado";
    public static final String KEY_FinTurno = "FinTurno";


    public int AlertId;
    public String NumeroA;
    public String FechaMarcacion;
    public String FechaEsperada;
    public String FechaProxima;
    public String FlagTiempo;
    public String MargenAceptado;
    public String LatitudA;
    public String LongitudA;
    public String EstadoA;
    public String EstadoBoton;
    public String FechaEsperadaIso;
    public String FechaEsperadaIsoFin;
    public String DispositivoId;
    public String CodigoEmpleado;
    public String FinTurno;
}
