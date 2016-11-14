package com.idslatam.solmar.View;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RegisterNumber extends Activity implements View.OnClickListener{

    Bundle b;
    String Id;
    protected String URL_API;

    EditText edNumero;
    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        Button btn_login;
        edNumero = (EditText)findViewById(R.id.EdFotocheck);
        btn_login=(Button)findViewById(R.id.btn_sendNumber);
        btn_login.setOnClickListener(this);

        b = getIntent().getExtras();
        Id = b.getString("Id");
    }

    @Override
    public void onClick(View v) {
        String Num = edNumero.getText().toString();

        Configuration configuration = new Configuration();
        configuration.NumeroCel = Num;
        configuration.ConfigurationId = 1;
        configurationCRUD.updateNumero(configuration);

        new PostAsync().execute(Id, Num);

    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;


        private final String URL = URL_API.concat("Dispositivo/PutNumber");//; "http://solmar.azurewebsites.net//Dispositivo/PutNumber";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(RegisterNumber.this);
            pDialog.setMessage("Gracias, en inst\u00E1ntes nos comunicar\u00E9mos contigo!");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("Id", args[0]);
                params.put("Numero", args[1]);

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST", params);

                if (json != null) {

                    Log.d("JSON result", json.toString());

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
            Intent i = new Intent(getApplicationContext(), Bienvenido.class );
            startActivity(i);

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Hecho!", message);
            }else{
                Log.d("Falló", message);
            }
        }

    }

}
