package com.idslatam.solmar.Pruebas.Data;

/**
 * Created by Luis on 14/11/2016.
 */

public class DataTrackingDetail {

    public int TrackingId;
    public String Numero;
    public String DispositivoId;
    public String FechaCelular;
    public String Latitud;
    public String Longitud;
    public String EstadoCoordenada;
    public String OrigenCoordenada;
    public String Velocidad;
    public String Bateria;
    public String Precision;
    public String SenialCelular;
    public String GpsHabilitado;
    public String WifiHabilitado;
    public String DatosHabilitado;
    public String ModeloEquipo;
    public String Imei;
    public String VersionApp;
    public String FechaAlarma;
    public String Time;
    public String ElapsedRealtimeNanos;
    public String Altitude;
    public String Bearing;
    public String Extras;
    public String Classx;
    public String Actividad;
    public String Valido;
    public String Intervalo;
    public String EstadoEnvio;
    public String FechaIso;


    public DataTrackingDetail(int trackingId, String numero, String dispositivoId, String fechaCelular, String latitud, String longitud, String estadoCoordenada, String origenCoordenada, String velocidad, String bateria, String precision, String senialCelular, String gpsHabilitado, String wifiHabilitado, String datosHabilitado, String modeloEquipo, String imei, String versionApp, String fechaAlarma, String time, String elapsedRealtimeNanos, String altitude, String bearing, String extras, String classx, String actividad, String valido, String intervalo, String estadoEnvio, String fechaIso) {
    //public DataTracking(int trackingId, String fechaCelular, String latitud, String longitud, String velocidad, String bateria, String precision, String gpsHabilitado, String wifiHabilitado, String datosHabilitado, String fechaAlarma, String time, String elapsedRealtimeNanos, String altitude, String bearing, String actividad, String valido, String intervalo, String estadoEnvio) {


        this.TrackingId = trackingId;
        this.Numero = numero;
        this.DispositivoId = dispositivoId;
        this.FechaCelular = fechaCelular;
        this.Latitud = latitud;
        this.Longitud = longitud;
        this.EstadoCoordenada = estadoCoordenada;
        this.OrigenCoordenada = origenCoordenada;
        this.Velocidad = velocidad;
        this.Bateria = bateria;
        this.Precision = precision;
        this.SenialCelular = senialCelular;
        this.GpsHabilitado = gpsHabilitado;
        this.WifiHabilitado = wifiHabilitado;
        this.DatosHabilitado = datosHabilitado;
        this.ModeloEquipo = modeloEquipo;
        this.Imei = imei;
        this.VersionApp = versionApp;
        this.FechaAlarma = fechaAlarma;
        this.Time = time;
        this.ElapsedRealtimeNanos = elapsedRealtimeNanos;
        this.Altitude = altitude;
        this.Bearing = bearing;
        this.Extras = extras;
        this.Classx = classx;
        this.Actividad = actividad;
        this.Valido = valido;
        this.Intervalo = intervalo;
        this.EstadoEnvio = estadoEnvio;
        this.FechaIso = fechaIso;
    }

    public int getTrackingId() {
        return TrackingId;
    }

    public void setTrackingId(int trackingId) {
        TrackingId = trackingId;
    }

    public String getFechaCelular() {
        return FechaCelular;
    }

    public void setFechaCelular(String fechaCelular) {
        FechaCelular = fechaCelular;
    }

    public String getLatitud() {
        return Latitud;
    }

    public void setLatitud(String latitud) {
        Latitud = latitud;
    }

    public String getLongitud() {
        return Longitud;
    }

    public void setLongitud(String longitud) {
        Longitud = longitud;
    }

    public String getVelocidad() {
        return Velocidad;
    }

    public void setVelocidad(String velocidad) {
        Velocidad = velocidad;
    }

    public String getBateria() {
        return Bateria;
    }

    public void setBateria(String bateria) {
        Bateria = bateria;
    }

    public String getPrecision() {
        return Precision;
    }

    public void setPrecision(String precision) {
        Precision = precision;
    }

    public String getGpsHabilitado() {
        return GpsHabilitado;
    }

    public void setGpsHabilitado(String gpsHabilitado) {
        GpsHabilitado = gpsHabilitado;
    }

    public String getWifiHabilitado() {
        return WifiHabilitado;
    }

    public void setWifiHabilitado(String wifiHabilitado) {
        WifiHabilitado = wifiHabilitado;
    }

    public String getDatosHabilitado() {
        return DatosHabilitado;
    }

    public void setDatosHabilitado(String datosHabilitado) {
        DatosHabilitado = datosHabilitado;
    }

    public String getFechaAlarma() {
        return FechaAlarma;
    }

    public void setFechaAlarma(String fechaAlarma) {
        FechaAlarma = fechaAlarma;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getElapsedRealtimeNanos() {
        return ElapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(String elapsedRealtimeNanos) {
        ElapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public String getAltitude() {
        return Altitude;
    }

    public void setAltitude(String altitude) {
        Altitude = altitude;
    }

    public String getBearing() {
        return Bearing;
    }

    public void setBearing(String bearing) {
        Bearing = bearing;
    }

    public String getActividad() {
        return Actividad;
    }

    public void setActividad(String actividad) {
        Actividad = actividad;
    }

    public String getValido() {
        return Valido;
    }

    public void setValido(String valido) {
        Valido = valido;
    }

    public String getIntervalo() {
        return Intervalo;
    }

    public void setIntervalo(String intervalo) {
        Intervalo = intervalo;
    }

    public String getEstadoEnvio() {
        return EstadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        EstadoEnvio = estadoEnvio;
    }

    public String getFechaIso() {
        return FechaIso;
    }

    public void getFechaIso(String fechaIso) {
        FechaIso = fechaIso;
    }

}
