package com.idslatam.solmar.People;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;

public class PeopleDetalle extends AppCompatActivity {

    Context mContext;

    LinearLayout people_detalle_mensaje;
    TextView people_txt_mensaje, people_detalle_txtfoto_valor, people_detalle_txtfoto_vehiculo;
    EditText people_detalle_dni, people_edt_persTipo, people_detalle_nombre, people_detalle_empresa,
            people_detalle_motivo, people_detalle_codArea;

    private static final int CAPTURE_MEDIA = 368;
    private Activity activity;

    String Uri_Foto, URL_API;

    boolean fotoValor = true, fotoVehiculo = true;

    Button people_detalle_btn_registrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_detalle);

        mContext = this;
        activity = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        people_detalle_mensaje = (LinearLayout)findViewById(R.id.people_detalle_mensaje);
        people_txt_mensaje = (TextView)findViewById(R.id.people_txt_mensaje);

        people_edt_persTipo = (EditText)findViewById(R.id.people_edt_persTipo);
        people_detalle_dni = (EditText) findViewById(R.id.people_detalle_dni);
        people_detalle_nombre = (EditText) findViewById(R.id.people_detalle_nombre);
        people_detalle_empresa = (EditText) findViewById(R.id.people_detalle_empresa);
        people_detalle_codArea = (EditText) findViewById(R.id.people_detalle_codArea);
        people_detalle_motivo = (EditText) findViewById(R.id.people_detalle_motivo);

        people_detalle_txtfoto_valor = (TextView)findViewById(R.id.people_detalle_txtfoto_valor);
        people_detalle_txtfoto_vehiculo = (TextView)findViewById(R.id.people_detalle_txtfoto_vehiculo);

        people_detalle_btn_registrar = (Button)findViewById(R.id.people_detalle_btn_registrar);

        Log.e(" PEOPLE DETALLE ", "onCreate");
        loadDatosPeople();

    }

    public void loadDatosPeople(){

        Log.e(" PEOPLE DETALLE ", "loadDatosPeople");

        String json = null, dni = null, fotoValor = null, fotoVehiculo = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoValor = c.getString(c.getColumnIndex("fotoValor"));
                fotoVehiculo = c.getString(c.getColumnIndex("fotoVehiculo"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Log.e(" PEOPLE dni ", dni);
        Log.e(" PEOPLE json ", json);

        if (json == null){return;}

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Log.e(" PEOPLE DETALLE ", json.toString());


        if (jsonObject.get("Resultado").getAsString().equalsIgnoreCase("OK")){
            people_detalle_mensaje.setBackgroundColor(Color.parseColor("#00796B"));
        } else {
            people_detalle_mensaje.setBackgroundColor(Color.parseColor("#FF5252"));
            people_detalle_btn_registrar.setEnabled(false);
        }

        people_txt_mensaje.setText(jsonObject.get("Header").getAsString());

        if (!jsonObject.get("persTipo").isJsonNull()){
            people_edt_persTipo.setText(jsonObject.get("persTipo").getAsString());
        }

        people_detalle_nombre.setText(jsonObject.get("persNombres").getAsString());
        people_detalle_empresa.setText(jsonObject.get("persEmpresa").getAsString());
        people_detalle_motivo.setText(jsonObject.get("persMotivo").getAsString());
        people_detalle_dni.setText(dni);
        people_detalle_codArea.setText(jsonObject.get("persArea").getAsString());

        if (fotoValor!=null){
            people_detalle_txtfoto_valor.setText("¡Foto valor guardada!");
        }

        if (fotoVehiculo!=null){
            people_detalle_txtfoto_vehiculo.setText("¡Foto vehículo guardada!");
        }

    }

    public void salir(View view){
        finish();
    }

    public void fotoValor(View view){
        fotoValor = true;
        fotoVehiculo = false;
        tomarFoto();
    }

    public void fotoVehiculo(View view){
        fotoValor = false;
        fotoVehiculo = true;
        tomarFoto();
    }

    public void tomarFoto(){

        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_MEDIUM)
                .enableImageCropping(false)
                .launchCamera();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {
            Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));

            String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            Uri_Foto = photoUri;

            if (fotoValor){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE People SET fotoValor = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoValor ","true");
                } catch (Exception eew){}


                people_detalle_txtfoto_valor.setText("¡Foto valor guardada!");

                //imgEstadoDelantera.setImageResource(R.drawable.ic_check_foto);
                fotoValor = false;

            } else if (fotoVehiculo){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE People SET fotoVehiculo = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoVehiculo ","true");
                } catch (Exception eew){}

                people_detalle_txtfoto_vehiculo.setText("¡Foto vehículo guardada!");

                //imgEstadoPaniramica.setImageResource(R.drawable.ic_check_foto);
                fotoVehiculo = false;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void guardarPeople(View view){

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(PeopleDetalle.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String dni = null, json = null, fotoVal = null, fotoVeh = null,
                GuidDipositivo = null;
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoVal = c.getString(c.getColumnIndex("fotoValor"));
                fotoVeh = c.getString(c.getColumnIndex("fotoVehiculo"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                GuidDipositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));

            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        Gson gson = new Gson();
        JsonObject result = gson.fromJson(json, JsonObject.class);

        Log.e("JsonObject SEND ", result.toString());


        String URL = URL_API.concat("api/Patrol/Create");

        /*Log.e("Numero ", Numero);
        Log.e("DispositivoId ", DispositivoId);
        Log.e("Placa ", Placa);
        Log.e("CargoTipoMovimientoId ", CargoTipoMovimiento);
        Log.e("CargoTipoCargaId ", TipoCarga);
        Log.e("Casco ", Casco);*/


        Ion.with(mContext)
                .load(URL)
                .setMultipartParameter("Doi", dni)
                .setMultipartParameter("DispositivoId", GuidDipositivo)
                //.setMultipartParameter("Placa", Placa)
                //.setMultipartParameter("CargoTipoMovimientoId", CargoTipoMovimiento)
                //.setMultipartParameter("CargoTipoCargaId", TipoCarga)
                .setMultipartFile("Valor", new File(fotoVal))
                .setMultipartFile("Vehiculo", new File(fotoVeh))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            limpiarDatos();

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                            try {
                                showDialogSend();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });
    }

    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    public boolean limpiarDatos(){

        try {

            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE People SET fotoVehiculo = " + null);
            dba.execSQL("UPDATE People SET fotoValor = " + null);
            dba.close();

        } catch (Exception eew){}

        return true;
    }

}
