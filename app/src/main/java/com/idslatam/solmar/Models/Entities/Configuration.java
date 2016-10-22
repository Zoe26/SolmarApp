package com.idslatam.solmar.Models.Entities;

/**
 * Created by Luis on 22/10/2016.
 */

public class Configuration {

    public static final String TABLE_CONFIGURATION = "Configuration";

    public static final String KEY_ID_CONFIGURATION = "ConfigurationId";
    public static final String KEY_GuidDipositivo = "GuidDipositivo";
    public static final String KEY_EstaAcceso = "EstaActivado";
    public static final String KEY_NumeroCel = "NumeroCel";
    public static final String KEY_Token = "Token";
    public static final String KEY_EstadoAprobado = "EstadoAprobado";
    public static final String KEY_OutApp = "OutApp";
    public static final String KEY_IntervaloTracking = "IntervaloTracking";
    public static final String KEY_AsistenciaId= "AsistenciaId";
    public static final String KEY_CodigoEmpleado= "CodigoEmpleado";
    public static final String KEY_IntervaloTrackingSinConex = "IntervaloTrackingSinConex";
    public static final String KEY_IntervaloMarcacion = "IntervaloMarcacion";
    public static final String KEY_IntervaloMarcacionTolerancia = "IntervaloMarcacionTolerancia";
    public static final String KEY_VecesPresionarVolumen = "VecesPresionarVolumen";
    public static final String KEY_ContadorBp = "ContadorBp";
    public static final String KEY_NivelVolumen = "NivelVolumen";
    public static final String KEY_FechaMarcacionBp = "FechaMarcacionBp";
    public static final String KEY_FechaLimite = "FechaLimite";
    public static final String KEY_IntervaloTrackingEmergencia = "IntervaloTrackingEmergencia";
    public static final String KEY_OutPrecision = "OutPrecision";
    public static final String KEY_Precision = "Precision";
    public static final String KEY_ContadorLocation = "ContadorLocation";
    public static final String KEY_ContadorProvider = "ContadorProvider";
    public static final String KEY_FechaEjecucionAlarm = "FechaEjecucionAlarm";
    public static final String KEY_TipoActividad = "TipoActividad";


    public int ConfigurationId;
    public String GuidDispositivo;
    public String EstaAcceso;
    public String NumeroCel;
    public String Token;
    public String OutApp;
    public String EstadoAprobado;
    public int IntervaloTracking;
    public String AsistenciaId;
    public String CodigoEmpleado;
    public int IntervaloTrackingSinConex;
    public int IntervaloMarcacion;
    public int IntervaloMarcacionTolerancia;
    public int VecesPresionarVolumen;
    public int ContadorBp;
    public int NivelVolumen;
    public String FechaMarcacionBp;
    public int IntervaloTrackingEmergencia;
    public String FechaLimite;
    public int Precision;
    public int ContadorLocation;
    public int ContadorProvider;
    public String FechaEjecucionAlarm;
    public String TipoActividad;

}