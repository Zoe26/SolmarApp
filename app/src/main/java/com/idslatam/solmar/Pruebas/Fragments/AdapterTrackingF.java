package com.idslatam.solmar.Pruebas.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Adapters.AdapterTracking;
import com.idslatam.solmar.Pruebas.Data.DataTracking;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Fragments.HomeFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdapterTrackingF extends android.app.Fragment implements  View.OnClickListener{

    Context thiscontext;
    View myView;

    int _Tracking_Id =0;

    private ListView lvDatost;
    private AdapterTracking adapter;
    private List<DataTracking> mDataTracking;

    EditText hor;
    Button filtrar;

    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    int val;

    public static AdapterTrackingF newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        AdapterTrackingF imageFragment = new AdapterTrackingF();
        imageFragment.setArguments(args);

        return imageFragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thiscontext = container.getContext();
        myView = inflater.inflate(R.layout.tracking_list_view, container, false);

        findViewsById(myView);

        mDataTracking = new ArrayList<>();



        return myView;
    }

    private void findViewsById(View myView) {

        hor = (EditText)myView.findViewById(R.id.edtHora);
        filtrar = (Button) myView.findViewById(R.id.btnFiltrar);
        lvDatost = (ListView) myView.findViewById(R.id.tracking_ListView);

        String s = hor.getText().toString();

        if (s.matches("")) {
            val=1;
        }

        filtrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("FILTRO ", "CLICK");
                val = Integer.parseInt(hor.getText().toString() );
                Consult();
            }
        });
    }

    public void onClick(View v) {}

    public void Consult(){

        Log.e("CONSULT", "CLICK");

        Calendar current = Calendar.getInstance();
        current.set(Calendar.HOUR_OF_DAY, val);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);

        String strLong = formatoIso.format(current.getTime());

        Calendar currentL = Calendar.getInstance();
        currentL.set(Calendar.HOUR_OF_DAY, val);
        currentL.set(Calendar.MINUTE, 59);
        currentL.set(Calendar.SECOND, 59);

        String strLongL = formatoIso.format(currentL.getTime());;

        Log.e("FECHA F", strLong);
        Log.e("FECHA L", strLongL);

        mDataTracking = new ArrayList<>();

        String FechaCelular, Latitud, Longitud, Velocidad, Bateria, Precision, GpsHabilitado, WifiHabilitado,
                DatosHabilitado, FechaAlarma, Time, ElapsedRealtimeNanos, Altitude, Bearing, Actividad, Valido, Intervalo, EstadoEnvio;

        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
           /* String selectQuery = "SELECT TrackingId, FechaCelular, Latitud, Longitud, Velocidad, Bateria, Precision, GpsHabilitado" +
                    ", WifiHabilitado, DatosHabilitado, FechaAlarma, Time, ElapsedRealtimeNanos, Altitude, Bearing, Actividad, Valido" +
                    ", Intervalo, EstadoEnvio  FROM Tracking ORDER BY ElapsedRealtimeNanos DESC";
                    */

            String selectQuery = "SELECT TrackingId, FechaIso, ElapsedRealtimeNanos FROM Tracking WHERE FechaIso BETWEEN '"+strLong+"' AND '"+strLongL+"'";
            //String selectQuery = "SELECT TrackingId, FechaCelular FROM Tracking ORDER BY ElapsedRealtimeNanos DESC";

            Cursor c = db.rawQuery(selectQuery, new String[]{});

            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no haya más registros
                do {
                    _Tracking_Id = c.getInt(c.getColumnIndex("TrackingId"));

                    FechaCelular = c.getString(c.getColumnIndex("FechaIso"));
                    /*Latitud = c.getString(c.getColumnIndex("Latitud"));
                    Longitud = c.getString(c.getColumnIndex("Longitud"));
                    Velocidad = c.getString(c.getColumnIndex("Velocidad"));
                    Bateria = c.getString(c.getColumnIndex("Bateria"));
                    Precision = c.getString(c.getColumnIndex("Precision"));
                    GpsHabilitado = c.getString(c.getColumnIndex("GpsHabilitado"));
                    WifiHabilitado = c.getString(c.getColumnIndex("WifiHabilitado"));
                    DatosHabilitado = c.getString(c.getColumnIndex("DatosHabilitado"));
                    FechaAlarma = c.getString(c.getColumnIndex("FechaAlarma"));
                    Time = c.getString(c.getColumnIndex("Time"));
                    ElapsedRealtimeNanos = c.getString(c.getColumnIndex("ElapsedRealtimeNanos"));
                    Altitude = c.getString(c.getColumnIndex("Altitude"));
                    Bearing = c.getString(c.getColumnIndex("Bearing"));
                    Actividad = c.getString(c.getColumnIndex("Actividad"));
                    Valido = c.getString(c.getColumnIndex("Valido"));
                    Intervalo = c.getString(c.getColumnIndex("Intervalo"));
                    EstadoEnvio = c.getString(c.getColumnIndex("EstadoEnvio"));
                    */

                    Log.e("FILTRO ", FechaCelular);

                    mDataTracking.add(new DataTracking(_Tracking_Id, "FechaIso: "+ FechaCelular));

                    /*mDataTracking.add(new DataTracking(_Tracking_Id, "FechaCelular: "+ FechaCelular, "Latitud: "+ Latitud, "Longitud: "+Longitud,
                            "Velocidad (m/s): "+Velocidad, "Bateria: "+Bateria, "Precision: "+Precision, "GpsHabilitado: "+GpsHabilitado, "WifiHabilitado: "+WifiHabilitado,
                            "DatosHabilitado: "+DatosHabilitado, "FechaAlarma: "+FechaAlarma, "Time: "+Time, "ElapsedRealtimeNanos: "+ElapsedRealtimeNanos, "Altitude: "+Altitude,
                            "Bearing: "+ Bearing, "Actividad: "+ Actividad, "Valido: "+ Valido, "Intervalo: "+ Intervalo, "EstadoEnvio: "+EstadoEnvio));
                            */
                } while(c.moveToNext());
            }

        }catch (Exception e){
            Log.e("EXCPTIO", e.toString());
        }


        Log.e("EXCPTIO", "LAST");

        adapter = new AdapterTracking(thiscontext, mDataTracking);
        lvDatost.setAdapter(adapter);
    }

}

