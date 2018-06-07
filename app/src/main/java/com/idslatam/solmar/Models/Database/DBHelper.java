package com.idslatam.solmar.Models.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.Models.Entities.Aplicaciones;
import com.idslatam.solmar.Models.Entities.Asistencia;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.CargoFoto;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.Models.Entities.Contactos;
import com.idslatam.solmar.Models.Entities.Menu;
import com.idslatam.solmar.Models.Entities.PatrolContenedor;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;
import com.idslatam.solmar.Models.Entities.People;
import com.idslatam.solmar.Models.Entities.SettingsPermissions;
import com.idslatam.solmar.Models.Entities.Tracking;
import com.idslatam.solmar.Pruebas.Entities.AlarmTrack;

/**
 * Created by Luis on 22/10/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "SolgisDB";

    public DBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_TRACKING = "CREATE TABLE " + Tracking.TABLE  + "("
                + Tracking.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Tracking.KEY_Numero + " TEXT,"
                + Tracking.KEY_DispositivoId + " TEXT,"
                + Tracking.KEY_FechaCelular + " TEXT, "
                + Tracking.KEY_Latitud + " TEXT,"
                + Tracking.KEY_Longitud + " TEXT,"
                + Tracking.KEY_EstadoCoordenada + " TEXT,"
                + Tracking.KEY_OrigenCoordenada + " TEXT,"
                + Tracking.KEY_Velocidad + " TEXT,"
                + Tracking.KEY_Bateria + " TEXT,"
                + Tracking.KEY_Precision + " TEXT,"
                + Tracking.KEY_SenialCelular + " TEXT,"
                + Tracking.KEY_GpsHabilitado + " TEXT,"
                + Tracking.KEY_WifiHabilitado + " TEXT,"
                + Tracking.KEY_DatosHabilitado + " TEXT,"
                + Tracking.KEY_ModeloEquipo + " TEXT,"
                + Tracking.KEY_Imei + " TEXT,"
                + Tracking.KEY_VersionApp + " TEXT,"
                + Tracking.KEY_FechaEjecucionAlarm + " TEXT,"
                + Tracking.KEY_Time + " TEXT, "
                + Tracking.KEY_ElapsedRealtimeNanos + " TEXT, "
                + Tracking.KEY_Altitude + " TEXT, "
                + Tracking.KEY_Bearing + " TEXT, "
                + Tracking.KEY_Extras + " TEXT, "
                + Tracking.KEY_Classx + " TEXT, "
                + Tracking.KEY_Actividad + " TEXT, "
                + Tracking.KEY_Valido + " TEXT, "
                + Tracking.KEY_Intervalo + " TEXT, "
                + Tracking.KEY_EstadoEnvio + " TEXT, "
                + Tracking.KEY_FechaIso+ " TEXT)";


        String CREATE_TABLE_CONFIGURATION = "CREATE TABLE " + Configuration.TABLE_CONFIGURATION  + "("
                + Configuration.KEY_ID_CONFIGURATION  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Configuration.KEY_GuidDipositivo + " TEXT,"
                + Configuration.KEY_EstaAcceso + " TEXT, "
                + Configuration.KEY_NumeroCel + " TEXT, "
                + Configuration.KEY_Token + " TEXT, "
                + Configuration.KEY_EstadoAprobado + " TEXT,"
                + Configuration.KEY_BPFechaInicio + " TEXT,"
                + Configuration.KEY_BPFechaFin + " TEXT,"
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
                + Configuration.KEY_Precision + " INTEGER,"
                + Configuration.KEY_ContadorLocation + " INTEGER,"
                + Configuration.KEY_ContadorProvider + " INTEGER,"
                + Configuration.KEY_FechaEjecucionAlarm + " INTEGER,"
                + Configuration.KEY_TipoActividad + " TEXT,"
                + Configuration.KEY_Latitud + " TEXT,"
                + Configuration.KEY_Longitud + " TEXT,"
                + Configuration.KEY_FechaInicioIso + " TEXT,"
                + Configuration.KEY_FechaSendIso + " TEXT,"
                + Configuration.KEY_FechaAlarmaIso + " TEXT,"
                + Configuration.KEY_FlagUpdate + " TEXT,"
                + Configuration.KEY_FlagSave + " TEXT,"
                + Configuration.KEY_EstadoSignalr + " TEXT,"
                + Configuration.KEY_ClienteId + " TEXT,"
                + Configuration.KEY_ContadorPulsacion + " INTEGER,"
                + Configuration.KEY_ContadorAux + " INTEGER,"
                + Configuration.KEY_isScreen + " TEXT,"
                + Configuration.KEY_SimSerie + " TEXT,"
                + Configuration.KEY_ContenedorPatrol + " TEXT,"
                + Configuration.KEY_ContenedorId + " TEXT,"
                + Configuration.KEY_Sesion + " TEXT,"
                + Configuration.KEY_Indice + " INTEGER,"
                + Configuration.KEY_Posicion + " INTEGER,"
                + Configuration.KEY_FechaBP + " TEXT,"
                + Configuration.KEY_IntervaloTrackingEmergencia + " INTEGER)";

        String CREATE_TABLE_ASISTENCIA = "CREATE TABLE " + Asistencia.TABLE_ASISTENCIA + "("
                + Asistencia.KEY_ID_Asistencia  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Asistencia.KEY_Numero + " TEXT, "
                + Asistencia.KEY_Asistencia + " TEXT, "
                + Asistencia.KEY_DispositivoId + " TEXT, "
                + Asistencia.KEY_FechaInicio + " TEXT, "
                + Asistencia.KEY_FechaTermino + " TEXT, "
                + Asistencia.KEY_Fotocheck + " TEXT)";

        String CREATE_TABLE_ALERT= "CREATE TABLE " + Alert.TABLE_ALERT  + "("
                + Alert.KEY_ID_ALERT  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Alert.KEY_NumeroA + " TEXT,"
                + Alert.KEY_FechaMarcacion + " TEXT,"
                + Alert.KEY_FechaEsperada + " TEXT, "
                + Alert.KEY_FechaProxima + " TEXT, "
                + Alert.KEY_FlagTiempo + " TEXT, "
                + Alert.KEY_MargenAceptado + " TEXT, "
                + Alert.KEY_LatitudA + " TEXT, "
                + Alert.KEY_LongitudA + " TEXT, "
                + Alert.KEY_EstadoA + " TEXT, "
                + Alert.KEY_EstadoBoton + " TEXT, "
                + Alert.KEY_FechaEsperadaIso + " TEXT, "
                + Alert.KEY_FechaEsperadaIsoFin + " TEXT, "
                + Alert.KEY_DispositivoId + " TEXT, "
                + Alert.KEY_CodigoEmpleado + " TEXT, "
                + Alert.KEY_FinTurno + " TEXT)";

        String CREATE_TABLE_SETTING_PERMISSION= "CREATE TABLE " + SettingsPermissions.TABLE_SETTING_PERMISSIONS + "("
                + SettingsPermissions.KEY_ID_SettingsPermissions  + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SettingsPermissions.KEY_Nombre + " TEXT,"
                + SettingsPermissions.KEY_Estado + " TEXT)";

        String CREATE_TABLE_ALARM_TRACK = "CREATE TABLE " + AlarmTrack.TABLE_ALARM_TRACK + "("
                + AlarmTrack.KEY_ID_ALARM_TRACK + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AlarmTrack.KEY_FechaAlarm + " TEXT,"
                + AlarmTrack.KEY_Estado + " TEXT)";

        String CREATE_TABLE_CONTACTOS = "CREATE TABLE " + Contactos.TABLE_CONTACTOS + "("
                + Contactos.KEY_ID_CONTACTOS + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contactos.KEY_Nombre + " TEXT,"
                + Contactos.KEY_PrimerNumero + " INTEGER,"
                + Contactos.KEY_SegundoNumero + " INTEGER)";

        String CREATE_TABLE_MENU = "CREATE TABLE " + Menu.TABLE_MENU + "("
                + Menu.KEY_ID_Menu + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Menu.KEY_Code + " TEXT,"
                + Menu.KEY_Nombre + " TEXT)";


        String CREATE_TABLE_CARGO = "CREATE TABLE " + Cargo.TABLE_CARGO + "("
                + Cargo.KEY_ID_Cargo + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Cargo.KEY_Initial + " TEXT,"
                + Cargo.KEY_Placa + " TEXT,"
                + Cargo.KEY_NombrePlaca + " TEXT,"
                + Cargo.KEY_TipoCarga + " TEXT,"
                + Cargo.KEY_EppCasco + " TEXT,"
                + Cargo.KEY_EppChaleco + " TEXT,"
                + Cargo.KEY_EppBotas + " TEXT,"
                + Cargo.KEY_Dni + " TEXT,"
                + Cargo.KEY_isLicencia + " TEXT,"
                + Cargo.KEY_NroOR + " TEXT,"
                + Cargo.KEY_isIngreso + " TEXT,"
                + Cargo.KEY_isCarga + " TEXT,"
                + Cargo.KEY_CantidadBultos + " TEXT,"
                + Cargo.KEY_fotoDelantera + " TEXT,"
                + Cargo.KEY_fotoTracera + " TEXT,"
                + Cargo.KEY_fotoPanoramica + " TEXT,"
                + Cargo.KEY_tamanoContenedor + " TEXT,"
                + Cargo.KEY_codigoContenedor + " TEXT,"
                + Cargo.KEY_numeroPrecintos + " TEXT,"
                + Cargo.KEY_origenDestino + " TEXT,"
                + Cargo.KEY_tipoDocumento + " TEXT,"
                + Cargo.KEY_numeroDocumento + " TEXT,"
                + Cargo.KEY_GuiaRemision + " TEXT,"
                + Cargo.KEY_json + " TEXT)";

        String CREATE_TABLE_CARGO_PRECINTO = "CREATE TABLE " + CargoPrecinto.TABLE_CARGO_PRECINTO + "("
                + CargoPrecinto.KEY_ID_CargoPrecinto + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CargoPrecinto.KEY_indice + " TEXT,"
                + CargoPrecinto.KEY_Foto + " TEXT)";

        String CREATE_TABLE_PATROL_PRECINTO = "CREATE TABLE " + PatrolPrecinto.TABLE_PATROL_PRECINTO + "("
                + PatrolPrecinto.KEY_ID_PatrolPrecinto + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PatrolPrecinto.KEY_indice + " TEXT,"
                + PatrolPrecinto.KEY_Foto + " TEXT)";

        String CREATE_TABLE_PATROL_CONTENEDOR = "CREATE TABLE " + PatrolContenedor.TABLE_PATROL_CONTENEDOR + "("
                + PatrolContenedor.KEY_ID_PatrolContenedor + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PatrolContenedor.KEY_ContenedorId + " TEXT,"
                + PatrolContenedor.KEY_Codigo + " TEXT)";

        String CREATE_TABLE_PEOPLE = "CREATE TABLE " + People.TABLE_PEOPLE + "("
                + People.KEY_ID_People + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + People.KEY_Initial + " TEXT,"
                + People.KEY_dni + " TEXT,"
                + People.KEY_json + " TEXT,"
                + People.KEY_fotoValor + " TEXT,"
                + People.KEY_fotoVehiculo + " TEXT,"
                + People.KEY_fotoVehiculoGuantera + " TEXT,"
                + People.KEY_fotoVehiculoMaletera + " TEXT)";

        String CREATE_TABLE_APLICACIONES = "CREATE TABLE " + Aplicaciones.TABLE_APLICACIONES + "("
                + Aplicaciones.KEY_ID_Aplicaciones + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Aplicaciones.KEY_Nombre + " TEXT)";

        String CREATE_TABLE_CARGO_FOTO = "CREATE TABLE " + CargoFoto.TABLE_NAME + "("
                + CargoFoto.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CargoFoto.KEY_CODIGO_SINCRONIZACION + " TEXT,"
                + CargoFoto.KEY_TIPO_FOTO + " TEXT,"
                + CargoFoto.KEY_INDICE + " TEXT,"
                + CargoFoto.KEY_FILE_PATH + " TEXT)";

        db.execSQL(CREATE_TABLE_APLICACIONES);
        db.execSQL(CREATE_TABLE_TRACKING);
        db.execSQL(CREATE_TABLE_CONFIGURATION);
        db.execSQL(CREATE_TABLE_ASISTENCIA);
        db.execSQL(CREATE_TABLE_ALERT);
        db.execSQL(CREATE_TABLE_SETTING_PERMISSION);
        db.execSQL(CREATE_TABLE_ALARM_TRACK);
        db.execSQL(CREATE_TABLE_CONTACTOS);
        db.execSQL(CREATE_TABLE_MENU);
        db.execSQL(CREATE_TABLE_CARGO);
        db.execSQL(CREATE_TABLE_CARGO_PRECINTO);
        db.execSQL(CREATE_TABLE_PATROL_PRECINTO);
        db.execSQL(CREATE_TABLE_PATROL_CONTENEDOR);
        db.execSQL(CREATE_TABLE_PEOPLE);
        db.execSQL(CREATE_TABLE_CARGO_FOTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Tracking.TABLE);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Configuration.TABLE_CONFIGURATION);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Asistencia.TABLE_ASISTENCIA);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Alert.TABLE_ALERT);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + SettingsPermissions.TABLE_SETTING_PERMISSIONS);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + AlarmTrack.TABLE_ALARM_TRACK);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Contactos.TABLE_CONTACTOS);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Menu.TABLE_MENU);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Cargo.TABLE_CARGO);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + CargoPrecinto.TABLE_CARGO_PRECINTO);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + PatrolPrecinto.TABLE_PATROL_PRECINTO);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + PatrolContenedor.TABLE_PATROL_CONTENEDOR);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + People.TABLE_PEOPLE);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + Aplicaciones.TABLE_APLICACIONES);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + CargoFoto.TABLE_NAME);
        onCreate(db);

    }
}
