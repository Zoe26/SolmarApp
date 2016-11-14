package com.idslatam.solmar.Pruebas.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdapterTrackingF extends android.app.Fragment implements  View.OnClickListener{

    Context thiscontext;
    View myView;

    int _Tracking_Id =0;

    private ListView lvDatost;
    private AdapterTracking adapter;
    private List<DataTracking> mDataTracking;

    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    EditText fromhor, frommin, fromDia, fromMes;
    EditText tohor, tomin;
    Button filtrar;

    int intfromH, intfromM, intfromDia, intfromMes ;
    int toH, toM, toD, tiMesint ;

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
        //++++++++++
        fromhor = (EditText)myView.findViewById(R.id.edtHora);
        frommin = (EditText)myView.findViewById(R.id.edtMin);

        tohor = (EditText)myView.findViewById(R.id.ToedtHora);
        tomin = (EditText)myView.findViewById(R.id.ToedtMin);

        fromDia = (EditText)myView.findViewById(R.id.edtDia);
        fromMes = (EditText)myView.findViewById(R.id.edtMes);

        filtrar = (Button) myView.findViewById(R.id.btnFiltrar);
        lvDatost = (ListView) myView.findViewById(R.id.tracking_ListView);

        Calendar currentF = Calendar.getInstance();

        int month = currentF.get(Calendar.MONTH);
        int dia = currentF.get(Calendar.DAY_OF_MONTH);

        String d = String.valueOf(dia);
        String m = String.valueOf(month);

        fromDia.setText(d);
        fromMes.setText(m);

        fromhor.setText("00");
        frommin.setText("00");
        tohor.setText("23");
        tomin.setText("59");

        filtrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("FILTRO ", "CLICK");
                intfromH = Integer.parseInt(fromhor.getText().toString());
                intfromM = Integer.parseInt(frommin.getText().toString());
                intfromDia = Integer.parseInt(fromDia.getText().toString());
                intfromMes = Integer.parseInt(fromMes.getText().toString());

                toH = Integer.parseInt(tohor.getText().toString() );
                toM = Integer.parseInt(tomin.getText().toString() );

                Consult();
            }
        });
    }

    public void onClick(View v) {}

    public void Consult(){

        Log.e("CONSULT", "CLICK");

        Calendar current = Calendar.getInstance();
        current.set(Calendar.MONTH, intfromMes);
        current.set(Calendar.DAY_OF_MONTH, intfromDia);
        current.set(Calendar.HOUR_OF_DAY, intfromH);
        current.set(Calendar.MINUTE, intfromM);
        current.set(Calendar.SECOND, 0);

        String strLong = formatoIso.format(current.getTime());

        Calendar currentL = Calendar.getInstance();
        currentL.set(Calendar.MONTH, intfromMes);
        currentL.set(Calendar.DAY_OF_MONTH, intfromDia);
        currentL.set(Calendar.HOUR_OF_DAY, toH);
        currentL.set(Calendar.MINUTE, toM);
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

            String selectQuery = "SELECT TrackingId, FechaIso, ElapsedRealtimeNanos FROM Tracking WHERE FechaIso BETWEEN '"+strLong+"' AND '"+strLongL+"' ORDER BY FechaIso DESC";
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

        lvDatost.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DataTracking person = (DataTracking) parent.getItemAtPosition(position);

                Log.e("FILTRO ", String.valueOf(person.getTrackingId()));

                //Toast.makeText(thiscontext, "P4: "+person.getTrackingId(), Toast.LENGTH_SHORT).show();

                int sf = person.getTrackingId();

                String FechaCelular = null, Latitud = null, Longitud = null, Velocidad = null, Bateria = null, Precision = null, GpsHabilitado = null, WifiHabilitado = null,
                        DatosHabilitado = null, FechaAlarma = null, Time = null, ElapsedRealtimeNanos = null, Altitude = null, Bearing = null, Actividad = null, Valido = null, Intervalo = null, EstadoEnvio = null;

                try {

                    DBHelper dataBaseHelper = new DBHelper(thiscontext);
                    SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
                    String selectQuery = "SELECT TrackingId, FechaCelular, Latitud, Longitud, Velocidad, Bateria, Precision, GpsHabilitado" +
                    ", WifiHabilitado, DatosHabilitado, FechaAlarma, Time, ElapsedRealtimeNanos, Altitude, Bearing, Actividad, Valido" +
                    ", Intervalo, EstadoEnvio, FechaIso FROM Tracking WHERE TrackingId = '"+sf+"'";

                    Cursor c = db.rawQuery(selectQuery, new String[]{});

                    if (c.moveToFirst()) {

                        FechaCelular = c.getString(c.getColumnIndex("FechaIso"));
                        Latitud = c.getString(c.getColumnIndex("Latitud"));
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


                    /*mDataTracking.add(new DataTracking(_Tracking_Id, "FechaCelular: "+ FechaCelular, "Latitud: "+ Latitud, "Longitud: "+Longitud,
                            "Velocidad (m/s): "+Velocidad, "Bateria: "+Bateria, "Precision: "+Precision, "GpsHabilitado: "+GpsHabilitado, "WifiHabilitado: "+WifiHabilitado,
                            "DatosHabilitado: "+DatosHabilitado, "FechaAlarma: "+FechaAlarma, "Time: "+Time, "ElapsedRealtimeNanos: "+ElapsedRealtimeNanos, "Altitude: "+Altitude,
                            "Bearing: "+ Bearing, "Actividad: "+ Actividad, "Valido: "+ Valido, "Intervalo: "+ Intervalo, "EstadoEnvio: "+EstadoEnvio));
                            */
                    }

                }catch (Exception e){
                    Log.e("EXCPTIO", e.toString());
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setMessage("FechaCelular: "+ FechaCelular + "| Latitud: "+ Latitud + "| Longitud: "+Longitud +
                        "| Velocidad (m/s): "+Velocidad + "| Bateria: "+Bateria + "| Precision: "+Precision + "| GpsHabilitado: "+GpsHabilitado + "| WifiHabilitado: "+WifiHabilitado +
                        "| DatosHabilitado: "+DatosHabilitado + "| FechaAlarma: "+FechaAlarma + "| Time: "+Time + "| ElapsedRealtimeNanos: "+ElapsedRealtimeNanos + "| Altitude: "+Altitude +
                        "| Bearing: "+ Bearing + "| Actividad: "+ Actividad + "| Valido: "+ Valido + "| Intervalo: "+ Intervalo + "| EstadoEnvio: "+EstadoEnvio);

                alertDialog.setNegativeButton("SALIR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                });

                AlertDialog dialog = alertDialog.create();
                dialog.show();

            }
        });

    }


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}

