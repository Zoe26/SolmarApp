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
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

public class People extends AppCompatActivity {

    EditText people_edt_dni;
    Context mContext;
    protected String URL_API;

    String variable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        people_edt_dni = (EditText)findViewById(R.id.people_edt_dni);



    }

    public void scanBarcode(View view) {
        new IntentIntegrator(this).initiateScan();
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

        String DispositivoId = null;

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

        } catch (Exception e) {}


        Log.e("DNI ", variable);
        Log.e("DispositivoId ", DispositivoId);

        String URL = URL_API.concat("api/People/VerificaDOI?NroDOI="+variable+"&DispositivoId="+DispositivoId+"");

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(People.this);
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

                                Intent i = new Intent(mContext, PeopleDetalle.class);
                                try {

                                    if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                                } catch (Exception esc){}

                                startActivity(i);

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

    public void salir(View view){
        finish();
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

    @Override
    protected void onStart() {
        super.onStart();


        String dni = null;

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT dni FROM People";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                dni = cConfiguration.getString(cConfiguration.getColumnIndex("dni"));
            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        if (dni==null){
            people_edt_dni.setText("");
            return;
        }

        people_edt_dni.setText(dni);

    }
}
