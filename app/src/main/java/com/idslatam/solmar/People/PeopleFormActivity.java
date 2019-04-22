package com.idslatam.solmar.People;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.DTO.People.Autenticacion.PeopleAutenticacionItemDTO;
import com.idslatam.solmar.Models.Entities.DTO.People.Autenticacion.PeopleAutenticacionTableDTO;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PeopleFormActivity extends AppCompatActivity {

    String DispositivoId = null, variable=null;
    Context mContext;
    protected String URL_API;
    LinearLayout lnlyOpciones;
    EditText people_edt_dni;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_form);

        lnlyOpciones = (LinearLayout) findViewById(R.id.lnlyOpciones);
        people_edt_dni = (EditText)findViewById(R.id.people_edt_dni);

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        LoadConfiguration();

        //
        try{
            GenerarBotonesAutenticacion();
        }catch (Exception e){
              Log.e("PeopleE A",e.toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            return;
        }

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.e("MainActivity", "Scanned "+result.getContents());
                people_edt_dni.setText(result.getContents());

                variable = result.getContents().toString();

                buscarPeople(variable);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void buscarPeople(View view){
        if(people_edt_dni.getText().toString().matches("")){
            Toast.makeText(this, "Ingrese DOI", Toast.LENGTH_SHORT).show();
            return;
        }

        variable = people_edt_dni.getText().toString();

        buscarPeople(variable);
    }

    public void buscarPeople(String variable){

        Log.e("DNI ", variable);
        Log.e("DispositivoId ", DispositivoId);

        String URL = URL_API.concat("api/People/VerificaDOI?NroDOI="+variable+"&DispositivoId="+DispositivoId+"");

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(PeopleFormActivity.this);
        pDialog.setMessage("Obteniendo Datos...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        Ion.with(this)
                .load("GET", URL)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if(e != null){

                            try {

                                if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                                Toast.makeText(mContext, "¡Ha ocurrido un problema!. Comuníquese con su administrador.", Toast.LENGTH_LONG).show();

                            } catch (Exception esc){}
                            return;

                        }

                        if(response == null){

                            Toast.makeText(mContext, "¡No hubo respuesta del servidor!. Por favor intente nuevamente. Caso contrario comuníquese con su administrador.", Toast.LENGTH_LONG).show();

                            try {

                                if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                            } catch (Exception esc){}
                            return;

                        }

                        if (response.getHeaders().code() == 200) {

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            String json = null;

                            Log.e("JsonObject PEOPLE ", result.toString());

                            if (!result.get("Resultado").isJsonNull()){

                                json = result.toString();
                                try {

                                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                    dba.execSQL("UPDATE People SET dni = '"+variable.toString()+"'");
                                    dba.execSQL("UPDATE People SET json = '"+json+"'");
                                    dba.close();

                                } catch (Exception edc){}

                                Intent i = new Intent(mContext, PeopleFormDetalleActivity.class);
                                try {

                                    if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                                } catch (Exception esc){}

                                startActivity(i);
                                finish();

                            } else {

                                mensajePersona();

                            }

                        } else {
                            Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
                        }

                        try {

                            if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                        } catch (Exception esc){}

                    }
                });

    }

    public void scanBarcode(View view){
        new IntentIntegrator(this).initiateScan();
    }

    public void salir(View view){

    }

    public void GenerarBotonesAutenticacion() throws Exception{
        if(DispositivoId!=null){
            final ProgressDialog pDialog;

            pDialog = new ProgressDialog(PeopleFormActivity.this);
            pDialog.setMessage("Obteniendo tipo de autenticación...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            String URL = URL_API.concat("api/People/Autenticacion?DispositivoId="+DispositivoId);

            Response<PeopleAutenticacionTableDTO> status;

            try {
                status = Ion.with(mContext)
                        .load("GET",URL)
                        .as(PeopleAutenticacionTableDTO.class)
                        .withResponse()
                        .get(25, TimeUnit.SECONDS);

                if (status.getHeaders().code() == 200) {
                    //return status.getResult();
                    Log.e("Data",  status.getResult().toString());
                    if(status.getResult().Estado){
                        for (PeopleAutenticacionItemDTO item : status.getResult().Data) {
                            Log.e("Data: "+item.Codigo,  item.Nombre);
                            switch (item.Codigo){
                                case "01":
                                    Button bth = new Button(this);
                                    bth.setText("Huella");
                                    bth.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                                    bth.setTextColor(getResources().getColor(R.color.icons));
                                    bth.setBackgroundColor(getResources().getColor(R.color.verde));

                                    bth.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //awesomeButtonClicked();
                                            //scanBarcode(v);
                                        }
                                    });



                                    lnlyOpciones.addView(bth);
                                    break;

                                case "02":
                                    Button bts = new Button(this);
                                    bts.setText("Escaner");
                                    bts.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                                    bts.setTextColor(getResources().getColor(R.color.icons));
                                    bts.setBackgroundColor(getResources().getColor(R.color.verde));



                                    bts.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //awesomeButtonClicked();
                                            scanBarcode(v);
                                        }
                                    });


                                    lnlyOpciones.addView(bts);
                                    break;
                            }
                        }
                    }
                }


                //return null;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {

                Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
            }

            pDialog.dismiss();

        }
    }

    public void LoadConfiguration(){
        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {
            DispositivoId = null;
        }


    }

    public void mensajePersona(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Persona no encontrada!. No se ha obtenido datos de ésta persona.");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }
}
