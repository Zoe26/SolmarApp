package com.idslatam.solmar.Models.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.Models.Entities.Tracking;

/**
 * Created by Luis on 22/10/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SolgisDB";

    public DBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_TRACKING = "CREATE TABLE " + Tracking.TABLE  + "("
                + Tracking.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Tracking.KEY_Numero + " TEXT,"
                + Tracking.KEY_FechaCelular + " TEXT, "
                + Tracking.KEY_Latitud + " TEXT,"
                + Tracking.KEY_Longitud + " TEXT,"
                + Tracking.KEY_Time + " TEXT, "
                + Tracking.KEY_Altitude + " TEXT, "
                + Tracking.KEY_Bearing + " TEXT, "
                + Tracking.KEY_Extras + " TEXT, "
                + Tracking.KEY_Class + " TEXT, "
                + Tracking.KEY_ElapsedRealtimeNanos + " TEXT, "
                + Tracking.KEY_EstadoCoordenada + " TEXT,"
                + Tracking.KEY_OrigenCoordenada + " TEXT,"
                + Tracking.KEY_Velocidad + " TEXT,"
                + Tracking.KEY_Bateria + " TEXT,"
                + Tracking.KEY_Presicion + " TEXT,"
                + Tracking.KEY_SenialCelular + " TEXT,"
                + Tracking.KEY_GpsHabilitado + " TEXT,"
                + Tracking.KEY_WifiHabilitado + " TEXT,"
                + Tracking.KEY_DatosHabilitado + " TEXT,"
                + Tracking.KEY_ModeloEquipo + " TEXT,"
                + Tracking.KEY_Imei + " TEXT,"
                + Tracking.KEY_VersionApp + " TEXT,"
                + Tracking.KEY_EstadoEnvio + " TEXT,"
                + Tracking.KEY_FechaCelularIso + " TEXT,"
                + Tracking.KEY_FechaLimite + " TEXT,"
                + Tracking.KEY_FechaDelayed + " TEXT,"
                + Tracking.KEY_FechaEjecucionAlarm + " TEXT)";

        String CREATE_TABLE_CONFIGURATION = "CREATE TABLE " + Configuration.TABLE_CONFIGURATION  + "("
                + Configuration.KEY_ID_CONFIGURATION  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Configuration.KEY_GuidDipositivo + " TEXT,"
                + Configuration.KEY_EstaAcceso + " TEXT, "
                + Configuration.KEY_NumeroCel + " TEXT, "
                + Configuration.KEY_Token + " TEXT, "
                + Configuration.KEY_EstadoAprobado + " TEXT,"
                + Configuration.KEY_OutApp + " TEXT,"
                + Configuration.KEY_CodigoEmpleado + " TEXT,"
                + Configuration.KEY_AsistenciaId + " TEXT,"
                + Configuration.KEY_IntervaloTracking + " INTEGER,"
                + Configuration.KEY_IntervaloTrackingSinConex + " INTEGER,"
                + Configuration.KEY_IntervaloMarcacion + " INTEGER,"
                + Configuration.KEY_IntervaloMarcacionTolerancia + " INTEGER,"
                + Configuration.KEY_VecesPresionarVolumen + " INTEGER,"
                + Configuration.KEY_ContadorBp + " INTEGER,"
                + Configuration.KEY_NivelVolumen + " INTEGER,"
                + Configuration.KEY_FechaMarcacionBp + " TEXT,"
                + Configuration.KEY_FechaLimite + " TEXT,"
                + Configuration.KEY_OutPrecision + " INTEGER,"
                + Configuration.KEY_Precision + " INTEGER,"
                + Configuration.KEY_ContadorLocation + " INTEGER,"
                + Configuration.KEY_ContadorProvider + " INTEGER,"
                + Configuration.KEY_FechaEjecucionAlarm + " INTEGER,"
                + Configuration.KEY_TipoActividad + " TEXT,"
                + Configuration.KEY_IntervaloTrackingEmergencia + " INTEGER)";

        db.execSQL(CREATE_TABLE_TRACKING);
        db.execSQL(CREATE_TABLE_CONFIGURATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Tracking.TABLE);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + Configuration.TABLE_CONFIGURATION);
        onCreate(db);

    }
}
