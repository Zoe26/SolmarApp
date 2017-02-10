package com.idslatam.solmar.View.Code;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Bienvenido;
import com.idslatam.solmar.View.MenuPrincipal;
import com.idslatam.solmar.View.Settings.AccessSettings;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CodeBar extends Activity {

    TextView contect, formatxt, currentNow;
    Spinner spinner_tipo;
    Calendar currenCodeBar;

    String valor,formato, NumeroCel, GuidDipositivo, CodigoEmpleado;

    protected String URL_API;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_bar);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        Intent intent = getIntent();
        valor = intent.getExtras().getString("epuzzle");
        formato = intent.getExtras().getString("format");
        currenCodeBar = Calendar.getInstance();


        spinner_tipo = (Spinner) findViewById(R.id.spinner_solgis);

        ArrayAdapter spinner_adapter = ArrayAdapter.createFromResource( this, R.array.tipo , android.R.layout.simple_spinner_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_tipo.setAdapter(spinner_adapter);

        contect = (TextView) findViewById(R.id.txt_content);
        formatxt = (TextView) findViewById(R.id.txt_format);
        currentNow = (TextView) findViewById(R.id.txt_fecha);

        contect.setText(valor);
        formatxt.setText("Formato: "+formato);
        currentNow.setText("Fecha: "+formatoIso.format(currenCodeBar.getTime()));

    }

    @Override
    public void onBackPressed() {
        scanBarcode();
        return;
    }

    public void backmenu(View view) {
        startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    public void enviarCode(View view) {
        int t = spinner_tipo.getSelectedItemPosition()+1;  //SelectedItem().toString();
        String fecha = formatoGuardar.format(currenCodeBar.getTime());
        String tipo = String.valueOf(t);

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo, CodigoEmpleado FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                NumeroCel = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                GuidDipositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
                CodigoEmpleado = cConfiguration.getString(cConfiguration.getColumnIndex("CodigoEmpleado"));

            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        Log.e("---! Send: TIPO "+tipo, " ! VALOR "+valor + " ! FECHA "+fecha+" ! FORMATO "+formato);

        if (isOnlineNet()){
            new PostAsync().execute(NumeroCel, GuidDipositivo, CodigoEmpleado, tipo, valor, fecha, formato);
        } else {
            pingRespuesta();
        }
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("api/CodigoBarra");
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(CodeBar.this);
            pDialog.setMessage("Enviando...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("Numero", args[0]);
                params.put("DispositivoId", args[1]);
                params.put("CodigoEmpleado", args[2]);
                params.put("CodigoBarraTipoDocumentoID", args[3]);
                params.put("Valor", args[4]);
                params.put("Fecha", args[5]);
                params.put("Formato", args[6]);
                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST", params);

                if (json != null) {

                    Log.e("CodeBar", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {

            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {

                try {

                    dialogoRespuesta(json);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Hecho!", message);
            }else {
                Log.d("Falló", message);
            }
        }

    }

    public void dialogoRespuesta(JSONObject jsonObject){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if(jsonObject.getString("Mensaje").equalsIgnoreCase("true")){
                builder.setTitle(Html.fromHtml("<font color='#4CAF50'>Respuesta de Servidor</font>"));
            } else {
                builder.setTitle(Html.fromHtml("<font color='#F44336'>Respuesta de Servidor</font>"));
            }

            //builder.setTitle("Respuesta de Servidor");
            builder.setMessage(jsonObject.getString("Mensaje"));
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();

                }
            });
            builder.show();

        } catch (Exception e){}
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void pingRespuesta(){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Conexión de Internet");
            builder.setMessage("Sin conexión a Internet. Por favor asegurese de tener conexión a internet");
            builder.setPositiveButton("Aceptar", null);
            builder.show();

        } catch (Exception e){}
    }

}
