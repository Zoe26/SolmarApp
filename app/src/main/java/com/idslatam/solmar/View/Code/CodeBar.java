package com.idslatam.solmar.View.Code;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Perfil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CodeBar extends Activity {

    TextView contect, formatxt, currentNow;
    Spinner spinner_tipo;
    Calendar currenCodeBar;

    String valor,formato, NumeroCel, GuidDipositivo, CodigoEmpleado;

    protected String URL_API;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    Context mContex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_bar);

        this.mContex = this;

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
        startActivity(new Intent(getBaseContext(), Perfil.class)
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

        if (true){

            String URL = URL_API.concat("api/CodigoBarra");


            ProgressDialog pDialog;

            pDialog = new ProgressDialog(CodeBar.this);
            pDialog.setMessage("Enviando...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            JsonObject json = new JsonObject();
            json.addProperty("Numero", NumeroCel);
            json.addProperty("DispositivoId", GuidDipositivo);
            json.addProperty("CodigoEmpleado", CodigoEmpleado);
            json.addProperty("CodigoBarraTipoDocumentoID", tipo);
            json.addProperty("Valor", valor);
            json.addProperty("Fecha", fecha);
            json.addProperty("Formato", formato);

            Ion.with(this)
                    .load("POST", URL)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> response) {

                            if(response == null){

                                Toast.makeText(mContex, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }

                                } catch (Exception edsv){}

                                return;

                            }

                            if(response.getHeaders().code() == 200){

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }

                                    dialogoRespuesta(result.get("Estado").getAsString() ,result.get("Header").getAsString(),result.get("Mensaje").getAsString());

                                } catch (Exception edd){

                                }
                                Log.e("JsonObject Bars ", response.toString());

                            } else  {

                                Toast.makeText(CodeBar.this, "Error al enviar Bars. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();
                                Log.e("Exception ", "Finaliza" );
                            }

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        }
                    });

        } else {
            //pingRespuesta();
        }
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    public void dialogoRespuesta(String Estado, String Header, String Mensaje){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CodeBar.this);

        if(Estado.equalsIgnoreCase("true")){

            mView = getLayoutInflater().inflate(R.layout.dialog_bars_ok, null);

        } else {

            mView = getLayoutInflater().inflate(R.layout.dialog_bars_failed, null);
        }

        TextView txtTitle = (TextView) mView.findViewById(R.id.title_bars_ok);
        TextView texMje = (TextView)mView.findViewById(R.id.mje_bars_ok);

        txtTitle.setText(Header);
        texMje.setText(Mensaje);

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    //dialog.dismiss();

                    startActivity(new Intent(getBaseContext(), Perfil.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

            if(Estado.equalsIgnoreCase("true")){
                builder.setTitle(Html.fromHtml("<font background-color='#FFFFFF' color='#FFFFF'>"+Header+"</font>"));
                builder.setMessage(Html.fromHtml("<font background-color='#FFFFF' color='#FFFFF'>"+Mensaje+"</font>"));
            } else {
                builder.setTitle(Html.fromHtml("<font color='#F44336'>"+Header+"</font>"));
                builder.setMessage(Html.fromHtml("<font background='#4CAF50' color='#4CAF50'>"+Mensaje+"</font>"));
            }

            //builder.setTitle("Respuesta de Servidor");

            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();

                }
            });
            builder.show();

        } catch (Exception e){}*/
    }

    public Boolean isOnlineNet() {

        try {
            Process p = Runtime.getRuntime().exec("ping 190.116.178.163:85");

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
