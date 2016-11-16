package com.idslatam.solmar.Pruebas.Data;

/**
 * Created by Luis on 16/11/2016.
 */

public class DataAlarmTrack {

    public int AlarmTrackId;
    public String FechaAlarm;
    public String Estado;

    public DataAlarmTrack(int alarmTrackId, String fechaAlarm, String estado) {
        this.AlarmTrackId = alarmTrackId;
        this.FechaAlarm = fechaAlarm;
        this.Estado = estado;
    }

    public int getAlarmTrackId() {
        return AlarmTrackId;
    }

    public void setAlarmTrackId(int alarmTrackId) {
        AlarmTrackId = alarmTrackId;
    }

    public String getFechaAlarm() {
        return FechaAlarm;
    }

    public void setFechaAlarm(String fechaAlarm) {
        FechaAlarm = fechaAlarm;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }

}
