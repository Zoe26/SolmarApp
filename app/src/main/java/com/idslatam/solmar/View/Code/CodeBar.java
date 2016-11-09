package com.idslatam.solmar.View.Code;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Bienvenido;
import com.idslatam.solmar.View.MenuPrincipal;
import com.idslatam.solmar.View.Settings.AccessSettings;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CodeBar extends Activity {

    TextView contect, formatxt, currentNow;
    Spinner spinner_tipo;
    Calendar currenCodeBar;

    String valor,formato;

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
        String tipo = spinner_tipo.getSelectedItem().toString();
        String fecha = formatoGuardar.format(currenCodeBar.getTime());

        new PostAsync().execute(tipo, valor, fecha, formato);
        Log.e("---! Send: TIPO "+tipo, " ! VALOR "+valor + " ! FECHA "+fecha+" ! FORMATO "+formato);

    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("api/dispositivo");
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

                params.put("Tipo", args[0]);
                params.put("Valor", args[1]);
                params.put("Fecha", args[2]);
                params.put("Formato", args[3]);
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

                        startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();

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
}
