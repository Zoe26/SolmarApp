package com.idslatam.solmar.View;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RegisterNumber extends Activity implements View.OnClickListener{

    Bundle b;
    String Id;
    protected String URL_API, serieSIM;

    Context mContex;

    EditText edNumero;
    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        this.mContex = this;

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

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT SimSerie FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                serieSIM = c.getString(c.getColumnIndex("SimSerie"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        try {

            String Num = edNumero.getText().toString();

            Configuration configuration = new Configuration();
            configuration.NumeroCel = Num;
            configuration.ConfigurationId = 1;
            configurationCRUD.updateNumero(configuration);

            String URL = URL_API.concat("api/Dispositivo/PutNumber");

            Log.e("Id ", Id);
            Log.e("Numero ", Num);
            Log.e("serieSIM ", serieSIM);

            JsonObject json = new JsonObject();
            json.addProperty("Id", Id);
            json.addProperty("Numero", Num);
            json.addProperty("SIMSerie", serieSIM);

            final ProgressDialog pDialog;

            pDialog = new ProgressDialog(RegisterNumber.this);
            pDialog.setMessage("Enviando N\u00famero...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

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
                                } catch (Exception eew){}

                                return;

                            }

                            if (response.getHeaders().code() == 200) {

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                Log.e("JsonObject ", result.toString());

                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception eew){}

                                Intent i = new Intent(getApplicationContext(), Bienvenido.class );
                                startActivity(i);

                            }

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception eew){}
                        }
                    });

        } catch (Exception e){}
    }

}
