package com.idslatam.solmar.View;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Alert.Services.ServicioAlerta;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Fragments.AdapterTrackingF;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.Fragments.HomeFragment;
import com.idslatam.solmar.View.Fragments.ImageFragment;
import com.idslatam.solmar.View.Fragments.JobsFragment;
import com.idslatam.solmar.View.Fragments.SampleFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.BottomBarFragment;
import com.roughike.bottombar.OnTabSelectedListener;

import com.idslatam.solmar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MenuPrincipal extends  ActionBarActivity {
    private BottomBar bottomBar;
    protected String URL_API;
    private Toolbar toolbar;
    String fotocheckCod;
    Bundle b;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        toolbar = (Toolbar) findViewById(R.id.app_bar);

        mContext = this;
        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT CodigoEmpleado FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                fotocheckCod = cConfiguration.getString(cConfiguration.getColumnIndex("CodigoEmpleado"));
            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        if(fotocheckCod==null) {

            try {

                b = getIntent().getExtras();
                fotocheckCod = b.getString("Fotoch");

            } catch (Exception e){}

//            fotocheckCod = Fotoch;
        }


        toolbar.setTitle("Solgis | "+fotocheckCod);
        setSupportActionBar(toolbar);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        bottomBar = BottomBar.attach(this, savedInstanceState);

        bottomBar.setFragmentItems(getSupportFragmentManager(), R.id.fragmentContainer,
                new BottomBarFragment(SampleFragment.newInstance(""), R.mipmap.ic_alert, "Alert"),
                new BottomBarFragment(ImageFragment.newInstance(""), R.mipmap.ic_image, "Image"),
                new BottomBarFragment(JobsFragment.newInstance(""), R.mipmap.ic_jobs, "Jobs"),
                new BottomBarFragment(HomeFragment.newInstance(""), R.mipmap.ic_home, "Home"),
                new BottomBarFragment(HomeFragment.newInstance(""), R.mipmap.ic_codebar, "Bars")
        );

        // Setting colors for different tabs when there's more than three of them.
        bottomBar.mapColorForTab(0, "#3B494C");
        bottomBar.mapColorForTab(1, "#00796B");
        bottomBar.mapColorForTab(2, "#7B1FA2");
        bottomBar.mapColorForTab(3, "#FF5252");
        bottomBar.mapColorForTab(4, "#3B494C");

        bottomBar.setOnItemSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                FragmentManager fragmentManager = getFragmentManager();
                switch (position) {
                    case 4:
                        // Item 4 Selected

                        try {

                            scanBarcode();
                            bottomBar.setDefaultTabPosition(0);

                        } catch (Exception e){}


                        break;

                }
            }
        });

        // Make a Badge for the first tab, with red background color and a value of "4".
        BottomBarBadge unreadMessages = bottomBar.makeBadgeForTabAt(2, "#E91E63", 4);

        // Control the badge's visibility
        unreadMessages.show();
        //unreadMessages.hide();

        // Change the displayed count for this badge.
        //unreadMessages.setCount(4);

        // Change the show / hide animation duration.
        unreadMessages.setAnimationDuration(200);

        // If you want the badge be shown always after unselecting the tab that contains it.
        //unreadMessages.setAutoShowAfterUnSelection(true);
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close) {
            try {
                showDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_salir) {
            try {
                showDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_tracking) {
            try {

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, new AdapterTrackingF());
                fragmentTransaction.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_configuration) {
            try {

                View mView = getLayoutInflater().inflate(R.layout.dialog_active_setting, null);
                EditText mPIN = (EditText) mView.findViewById(R.id.editTextPIN);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MenuPrincipal.this);
                mBuilder.setTitle("ACCESO CONFIGURACIONES");
                mBuilder.setMessage("Ingrese PIN de desbloqueo de configuraciones del tel\u00e9fono");
                mBuilder.setNegativeButton("SALIR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        d.cancel();
                    };
                });

                mBuilder.setPositiveButton("ACTIVAR", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface d, int arg1) {
                        if(mPIN.getText().toString().isEmpty()){
                            Toast.makeText(MenuPrincipal.this, "Ingrese PIN", Toast.LENGTH_SHORT).show();

                        } else {
                            String pin = mPIN.getText().toString();
                            if(pin.equals("s2016")){
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE SettingsPermissions SET Estado = 'true'");
                                dba.close();
                                Toast.makeText(MenuPrincipal.this, "Desbloqueado!", Toast.LENGTH_SHORT).show();

                            } else  {
                                mPIN.setText("");
                                Toast.makeText(MenuPrincipal.this, "PIN Incorrecto!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                });

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialog() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("¿Est\u00e1 seguro que desea salir?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new PostAsyncCerrarSesion().execute();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public class PostAsyncCerrarSesion extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(MenuPrincipal.this);
            dialog.setMessage("Finalizando...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            //do initialization of required objects objects here
        };
        @Override
        protected Void doInBackground(Void... params) {

            try {

                finalizarTurno();
                tareaLarga();
                finish();

            } catch (Exception e){}


//            Intent intent = new Intent(mainPerfil.this, LoginActivity.class);
//            startActivity(intent);

            try {

                startActivity(new Intent(MenuPrincipal.this, Login.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } catch (Exception e){}


//            finish();

            //do loading operation here
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }catch (Exception e){}
        }
    }

    private Boolean finalizarTurno(){

        try {

            stopService(new Intent(MenuPrincipal.this, ServicioAlerta.class));
            updateAlert();
            Asistencia();

        } catch (Exception e){}

        return true;
    }

    private void tareaLarga() {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}
    }

    public Boolean updateAlert(){

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("UPDATE Configuration SET Token = " + null);
            dbT.close();

        } catch (Exception e){}

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Alert SET FinTurno = 'true'");
            db.close();

        } catch (Exception e){}




//        DBHelper dataBaseHelper = new DBHelper(this);

        return true;
    }

    public Boolean Asistencia(){

        DBHelper dataBaseHelper = new DBHelper(this);

        int idUp = 0;
        String statusBoton = null, statusFinTurno = null;
        try {
//            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT AlertId, EstadoBoton, FinTurno FROM Alert";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToLast()) {
                idUp = cConfiguration.getInt(cConfiguration.getColumnIndex("AlertId"));
                statusBoton = cConfiguration.getString(cConfiguration.getColumnIndex("EstadoBoton"));
                statusFinTurno = cConfiguration.getString(cConfiguration.getColumnIndex("FinTurno"));
            }

            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {
            Log.e("Error Elminar U", e.getMessage());
        }

        Log.e("ID "+ String.valueOf(statusBoton) + " | Estado Btn "+ statusBoton, " | Estado Turno "+ statusFinTurno);

        if(statusBoton.equals("false") && statusFinTurno.equals("true")){

            try {

                SQLiteDatabase dbU = dataBaseHelper.getWritableDatabase();
                dbU.execSQL("DELETE FROM  Alert WHERE AlertId = "+idUp);
//            dbU.execSQL("UPDATE Alert SET FinTurno = 'false' WHERE AlertId = "+idUp);
                dbU.close();

            } catch (Exception e){}


            Log.e("Fin Consulta ", " Eliminar ");

        }

        //******************************************************************************************

        String AsistenciaId = null, DispositivoId=null, FechaTerminoCelular=null;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            FechaTerminoCelular = sdf.format(new Date());
//            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT AsistenciaId, GuidDipositivo FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToLast()) {

                AsistenciaId = c.getString(c.getColumnIndex("AsistenciaId"));
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));

            }
            c.close();
            db.close();

        }catch (Exception e){}

        new PostAsyncAsistencia().execute(AsistenciaId, DispositivoId, FechaTerminoCelular);

        return true;
    }

    class PostAsyncAsistencia extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("Attendance/end");//"http://solmar.azurewebsites.net/Attendance/end";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("AsistenciaId", args[0]);
                params.put("DispositivoId", args[1]);
                params.put("FechaTerminoCelular", args[2]);

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST",  params);

                if (json != null) {

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

            if (json != null) {

                Log.e("ASISTENCIA SALIDA ", json.toString());

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

    //**********************************************************************************************

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Intent i = new Intent(this, CodeBar.class);
                i.putExtra("epuzzle", result.getContents());
                i.putExtra("format", result.getFormatName());
                startActivity(i);
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}