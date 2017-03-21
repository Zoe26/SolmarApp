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
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
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

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

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
        try {

            String Num = edNumero.getText().toString();

            Configuration configuration = new Configuration();
            configuration.NumeroCel = Num;
            configuration.ConfigurationId = 1;
            configurationCRUD.updateNumero(configuration);

            String URL = URL_API.concat("Dispositivo/PutNumber");

            Log.e("Id ", Id);
            Log.e("Numero ", Num);

            Ion.with(this)
                    .load("POST", URL)
                    .setBodyParameter("Id", Id)
                    .setBodyParameter("Numero", Num)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if(result!=null){
                                Log.e("JsonObject ", result.toString());

                                Intent i = new Intent(getApplicationContext(), Bienvenido.class );
                                startActivity(i);

                            } else  {
                                Log.e("Exception ", "Finaliza "  + e.getMessage());
                            }
                        }
                    });

        } catch (Exception e){}
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try
        {
            if(!hasFocus)
            {
                Object service  = getSystemService("statusbar");
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse;

                //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse");
                    collapse.invoke(service);
                    collapse .setAccessible(true);
                    collapse .invoke(service);

                } else {
                    collapse = statusbarManager.getMethod("collapsePanels");
                    collapse.invoke(service);
                    collapse.setAccessible(true);
                    collapse.invoke(service);

                }


                //Method collapse = statusbarManager.getMethod("collapse");

            }
        }
        catch(Exception ex)
        {
            if(!hasFocus)
            {
                try {

                    Object service  = getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    Method collapse;

                    //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    if (currentApiVersion <= 16) {
                        collapse = statusbarManager.getMethod("collapse");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    } else {
                        collapse = statusbarManager.getMethod("collapsePanels");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }
    }

}
