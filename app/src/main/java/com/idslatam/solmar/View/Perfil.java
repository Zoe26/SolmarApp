package com.idslatam.solmar.View;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Alert.AlertActivity;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Apps.ApplicationAdapter;
import com.idslatam.solmar.BravoPapa.ScreenReceiver;
import com.idslatam.solmar.Cargo.CargoActivity;
import com.idslatam.solmar.Dialer.ContactosActivity;
import com.idslatam.solmar.ImageClass.Image;
import com.idslatam.solmar.Models.Crud.MenuCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Patrol.PatrolActivity;
import com.idslatam.solmar.People.People;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.Scan;
import com.idslatam.solmar.View.Fragments.SampleFragment;
import com.idslatam.solmar.View.PerfilSolmar.GridViewAdapter;
import com.idslatam.solmar.View.PerfilSolmar.Item;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.fragment;

public class Perfil extends AppCompatActivity implements AdapterView.OnItemClickListener {

    protected String URL_API;
    private Toolbar toolbar;
    String fotocheckCod;
    Bundle b;
    Context mContext;

    int _Menu_Id = 0;
    String DispositivoId, flagSesion;
    boolean state;

    private Point mSize;

    GridView gridview;
    GridViewAdapter gridviewAdapter;
    ArrayList<Item> data = new ArrayList<Item>();


    //private GridView gridview;
    private List<ApplicationInfo> applist = null;
    private PackageManager packageManager = null;
    private ApplicationAdapter listadaptor = null;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        mContext = this;

        packageManager = getPackageManager();

        //------------------------------------------------------------------------------------------
        try {

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            BroadcastReceiver mReceiver = new ScreenReceiver();

            this.getApplicationContext().registerReceiver(mReceiver, filter);

        } catch (IllegalArgumentException e) {
            Log.e("EXCEPTION REGISTER ", e.getMessage());
        }
        //------------------------------------------------------------------------------------------


        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT CodigoEmpleado, GuidDipositivo, Sesion FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                fotocheckCod = cConfiguration.getString(cConfiguration.getColumnIndex("CodigoEmpleado"));
                DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
                flagSesion = cConfiguration.getString(cConfiguration.getColumnIndex("Sesion"));
            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);

        b = getIntent().getExtras();
        state = b.getBoolean("State");


        toolbar.setTitle("Solgis | "+fotocheckCod);
        setSupportActionBar(toolbar);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        if (flagSesion==null){
            flagSesion = "false";
        }

        Log.e("flagSesion ", flagSesion);

        if(flagSesion.equalsIgnoreCase("false")){
            inicioTurno();
        }

        if (state == true) {
            load(savedInstanceState);
        } else {

            initView(); // Initialize the GUI Components
            fillData(); // Insert The Data
            setDataAdapter(); // Set the Data Adapter
        }

    }

    // Initialize the GUI Components
    private void initView()
    {
        gridview = (GridView) findViewById(R.id.gridView);
        gridview.setOnItemClickListener(this);
    }

    // Insert The Data
    private void fillData()
    {

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT Nombre, Code FROM Menu";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

                if (cConfiguration.moveToFirst()) {

                    do {

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("2")){
                            data.add(new Item("Alert", getResources().getDrawable(R.mipmap.aler_ic)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("4")){
                            data.add(new Item("Image", getResources().getDrawable(R.mipmap.ic_imagea)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("5")){
                            data.add(new Item("Jobs", getResources().getDrawable(R.mipmap.ic_imgjobs)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("6")){
                            data.add(new Item("Bars", getResources().getDrawable(R.mipmap.ic_barsa)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("7")){
                            data.add(new Item("Cargo", getResources().getDrawable(R.mipmap.ic_cargo)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("8")){
                            data.add(new Item("People", getResources().getDrawable(R.mipmap.ic_people)));
                        }

                        if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("9")){
                            data.add(new Item("Patrol", getResources().getDrawable(R.mipmap.ic_patrol)));
                        }

                    } while(cConfiguration.moveToNext());

                }

            cConfiguration.close();
            dbConfiguration.close();

            //data.add(new Item("People", getResources().getDrawable(R.mipmap.ic_people)));
            //data.add(new Item("Patrol", getResources().getDrawable(R.mipmap.ic_patrol)));
            data.add(new Item("Llamadas", getResources().getDrawable(R.mipmap.ic_llamada)));
            data.add(new Item("Mensajes", getResources().getDrawable(R.mipmap.ic_mje)));
            data.add(new Item("Configuración", getResources().getDrawable(R.mipmap.ic_settings)));

        } catch (Exception e) {}

        //checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

    }

    // Set the Data Adapter
    private void setDataAdapter()
    {
        gridviewAdapter = new GridViewAdapter(getApplicationContext(), R.layout.perfil_gridview, data);
        gridview.setAdapter(gridviewAdapter);
    }

    @Override
    public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long id)
    {
        packageManager = getPackageManager();

        if (data.get(position).getTitle().equalsIgnoreCase("Alert")){
            startActivity(new Intent(mContext, AlertActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Image")){
            startActivity(new Intent(mContext, Image.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Jobs")){

        }

        if (data.get(position).getTitle().equalsIgnoreCase("Bars")){
            startActivity(new Intent(mContext, Scan.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Llamadas")){
            startActivity(new Intent(mContext, ContactosActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Mensajes")){
            Intent intent = packageManager.getLaunchIntentForPackage("com.android.mms");
            if (null != intent) {
                startActivity(intent);
            }
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Cargo")){
            startActivity(new Intent(mContext, CargoActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("People")){
            startActivity(new Intent(mContext, People.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Patrol")){
            startActivity(new Intent(mContext, PatrolActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        if (data.get(position).getTitle().equalsIgnoreCase("Configuración")){

            try {

                View mView = getLayoutInflater().inflate(R.layout.dialog_active_setting, null);
                EditText mPIN = (EditText) mView.findViewById(R.id.editTextPIN);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Perfil.this);
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
                            Toast.makeText(Perfil.this, "Ingrese PIN", Toast.LENGTH_SHORT).show();

                        } else {
                            String pin = mPIN.getText().toString();
                            if(pin.equals("s2016")){

                                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                //Toast.makeText(MenuPrincipal.this, "Desbloqueado!", Toast.LENGTH_SHORT).show();

                            } else  {
                                mPIN.setText("");
                                Toast.makeText(Perfil.this, "PIN Incorrecto!", Toast.LENGTH_SHORT).show();
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
        }

        //String message = "Clicked : " + data.get(position).getTitle();
        //Toast.makeText(getApplicationContext(), message , Toast.LENGTH_SHORT).show();
    }

    public void load(Bundle savedInstanceState){

        String URL = URL_API.concat("Aplicacion/GetAppByUser?id="+DispositivoId);

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(Perfil.this);
        pDialog.setMessage("Cargando Configuraci\u00f3n...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        Ion.with(this)
                .load("GET", URL)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        //try catch here for null getHeaders

                        if(response == null){

                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Perfil.this, Login.class);

                            try {

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } catch (Exception ee){}

                            startActivity(intent);

                            return;
                        }

                        if (response.getHeaders().code() == 200) {

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                Log.e("JsonObject GET MENU ", result.toString());

                                JsonArray jarray = result.getAsJsonArray("Menu");

                                for (JsonElement pa : jarray) {
                                    JsonObject paymentObj = pa.getAsJsonObject();

                                    MenuCrud menuCrud = new MenuCrud(mContext);

                                    com.idslatam.solmar.Models.Entities.Menu menu = new com.idslatam.solmar.Models.Entities.Menu();
                                    menu.Nombre = paymentObj.get("Nombre").getAsString();
                                    menu.Code = paymentObj.get("Id").getAsString();
                                    menu.MenuId = _Menu_Id;
                                    _Menu_Id = menuCrud.insert(menu);

                                    Log.e("-- Nombre -- ", paymentObj.get("Nombre").getAsString());
                                    Log.e("-- Id -- ", paymentObj.get("Id").getAsString());

                                    JsonArray jarraconf = paymentObj.getAsJsonArray("Configuracion");

                                    for (JsonElement co : jarraconf) {
                                        JsonObject paymentC = co.getAsJsonObject();

                                        // ALERT
                                        if(paymentObj.get("Id").getAsString().equalsIgnoreCase("2")){

                                            if(paymentC.get("ConfiguracionId").getAsString().equalsIgnoreCase("3")){

                                                DBHelper dataBaseHelper = new DBHelper(mContext);
                                                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + paymentC.get("Valor").getAsString() + "'");
                                                db.close();

                                                Log.e("ALERT 3 ", paymentC.get("Valor").getAsString());

                                            }

                                            if(paymentC.get("ConfiguracionId").getAsString().equalsIgnoreCase("4")){

                                                DBHelper dataBaseHelper = new DBHelper(mContext);
                                                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + paymentC.get("Valor").getAsString()  + "'");
                                                db.close();

                                                Log.e("ALERT 4 ", paymentC.get("Valor").getAsString());
                                            }
                                        }

                                        // BRAVO PAPA
                                        if(paymentObj.get("Id").getAsString().equalsIgnoreCase("3")){

                                            if(paymentC.get("ConfiguracionId").getAsString().equalsIgnoreCase("5")){

                                                DBHelper dataBaseHelper = new DBHelper(mContext);
                                                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                db.execSQL("UPDATE Configuration SET VecesPresionarVolumen = '"+paymentC.get("Valor").getAsString()+"'");
                                                db.close();

                                                Log.e("BP 5 ", paymentC.get("Valor").getAsString());
                                            }
                                        }

                                    }

                                }

                                Log.e("-- Get Menu! ", " Finaliza");
                                //generarMenu(savedInstanceState);
                                initView(); // Initialize the GUI Components
                                fillData(); // Insert The Data
                                setDataAdapter(); // Set the Data Adapter

                            try {

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } catch (Exception ee){}

                        } else {

                            Intent intent = new Intent(Perfil.this, Login.class);

                            try {

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } catch (Exception ee){}

                            startActivity(intent);

                            Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();

                        }

                        try {

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        } catch (Exception ee){}
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_salir) {
            try {
                showDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_configuration) {
            try {

                View mView = getLayoutInflater().inflate(R.layout.dialog_active_setting, null);
                EditText mPIN = (EditText) mView.findViewById(R.id.editTextPIN);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Perfil.this);
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
                            Toast.makeText(Perfil.this, "Ingrese PIN", Toast.LENGTH_SHORT).show();

                        } else {
                            String pin = mPIN.getText().toString();
                            if(pin.equals("s2016")){

                                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);


                            } else  {
                                mPIN.setText("");
                                Toast.makeText(Perfil.this, "PIN Incorrecto!", Toast.LENGTH_SHORT).show();
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

        builder.setMessage("\u00BFEst\u00e1 seguro que desea salir?");

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

            try {
                dialog = new ProgressDialog(Perfil.this);
                dialog.setMessage("Finalizando...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                dialog.show();
            }catch (Exception d){}


        };

        @Override
        protected Void doInBackground(Void... params) {

            try {

                finalizarTurno(dialog);
                tareaLarga();

            } catch (Exception e){}

            try {

                //startActivity(new Intent(MenuPrincipal.this, Login.class)
                //        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                //finish();

            } catch (Exception e){}


//            finish();

            //do loading operation here
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {

                DBHelper dbgelperDeete = new DBHelper(mContext);
                SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
                sqldbDelete.execSQL("DELETE FROM Menu");
                sqldbDelete.close();

                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } catch (Exception dfs){}


            }catch (Exception e){}
        }
    }

    private Boolean finalizarTurno(ProgressDialog dialogo){

        try {
            updateAlert();

        } catch (Exception e){}

        try {
            Asistencia();

        } catch (Exception e){}

        try {
            cerrarSesion(dialogo);

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
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Configuration SET Sesion = 'false'");
            db.close();
        } catch (Exception e5) {}

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

        return true;
    }

    public Boolean Asistencia(){

        int idUp = 0;
        String statusBoton = null, statusFinTurno = null;
        try {
            DBHelper dataBaseHelperA = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelperA.getReadableDatabase();
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

        //Log.e("ID "+ String.valueOf(statusBoton) + " | Estado Btn "+ statusBoton, " | Estado Turno "+ statusFinTurno);

        if(statusBoton.equals("false") && statusFinTurno.equals("true")){

            try {
                DBHelper dataBaseHelperB = new DBHelper(this);
                SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                dbU.execSQL("DELETE FROM Alert WHERE AlertId = "+idUp);
                dbU.close();

            } catch (Exception e){}


            Log.e("Fin Consulta ", " Eliminar ");

        }

        return true;
    }

    public void cerrarSesion(ProgressDialog d){

        //******************************************************************************************
        String AsistenciaId = null, DispositivoId=null, FechaTerminoCelular=null;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            FechaTerminoCelular = sdf.format(new Date());
            DBHelper dataBaseHelper = new DBHelper(this);
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

        String cadenaA = "";
        String cadenaB = "";

        String[] ary = AsistenciaId.trim().split("-");
        String[] ary1 = DispositivoId.trim().split("-");

        for(int i=0;i<ary.length;i++){cadenaA = cadenaA +ary[i];}
        for(int i=0;i<ary1.length;i++){cadenaB = cadenaB +ary1[i];}

        //----------------------------------------------

        Log.e("cadenaA", cadenaA);
        Log.e("cadenaB", cadenaB);


        String URL = URL_API.concat("Attendance/end");

        JsonObject json = new JsonObject();
        json.addProperty("AsistenciaId", cadenaA);
        json.addProperty("DispositivoId", cadenaB);
        json.addProperty("FechaTerminoCelular", FechaTerminoCelular);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {

                        if (e!=null){
                            Log.e("Exception ", e.getMessage());
                            try {
                                if (d.isShowing()) {
                                    d.dismiss();
                                }
                            } catch (Exception dfs){}

                            finalizarTurnoX();
                            return;
                        }

                        if(result!=null){
                            Log.e("JSON SALIDA ", result.getResult().toString());


                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }

                        try {
                            if (d.isShowing()) {
                                d.dismiss();
                            }
                        } catch (Exception dfs){}
                        finalizarTurnoX();

                    }
                });

    }

    private Boolean finalizarTurnoX(){

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("UPDATE Configuration SET Token = " + null);
            dbT.close();

        } catch (Exception e){}

        try {

            Log.e("finalizarTurno ", "finalizarTurno");

            /*startActivity(new Intent(getBaseContext(), Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));*/

            Intent intent = new Intent(Perfil.this, Login.class);
            startActivity(intent);
            finish();

        } catch (Exception e){}

        return true;
    }

    public void inicioTurno(){


        String Num = null, DisId = null, CodigoEmpleado = null;

        String FechaInicioCelular = formatoGuardar.format(new Date());

        try {

            FechaInicioCelular = formatoGuardar.format(new Date());
            DBHelper dbhGUID = new DBHelper(mContext);
            SQLiteDatabase dbA = dbhGUID.getReadableDatabase();
            String selectQueryA = "SELECT NumeroCel, GuidDipositivo, CodigoEmpleado FROM Configuration";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToFirst()) {
                Num = cA.getString(cA.getColumnIndex("NumeroCel"));
                DisId = cA.getString(cA.getColumnIndex("GuidDipositivo"));
                CodigoEmpleado = cA.getString(cA.getColumnIndex("CodigoEmpleado"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){}

        String URL = URL_API.concat("api/Attendance");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", Num);
        json.addProperty("CodigoEmpleado", CodigoEmpleado);
        json.addProperty("DispositivoId", DisId);
        json.addProperty("FechaInicioCelular", FechaInicioCelular);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject response) {

                        if(response!=null){
                            Log.e("JsonObject Frag.", response.toString());

                            try {

                                DBHelper dataBaseHelper = new DBHelper(mContext);
                                SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                                dbT.execSQL("UPDATE Configuration SET AsistenciaId = '"+response.get("AsistenciaId").getAsString()+"'");
                                dbT.close();

                            } catch (Exception vdse){}

                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }

                        try {

                            DBHelper dataBaseHelper = new DBHelper(mContext);
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET Sesion = 'true'");
                            db.close();
                        } catch (Exception e5) {}
                    }
                });
    }


    /*private class LoadApplications extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            Log.e("list size", String.valueOf(applist.size()));
            listadaptor = new ApplicationAdapter(Perfil.this, R.layout.list_item, applist);
            System.out.println("adapter="+listadaptor);
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {

            //gridview.setAdapter(listadaptor);
            //gridview.setOnItemClickListener(MainActivity.this);

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {

                    Log.e("class name", String.valueOf(info.packageName));

                    *//*try {

                        DBHelper dataBaseHelper = new DBHelper(this);
                        SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
                        String selectQueryconfiguration = "SELECT Nombre, Code FROM Menu";
                        Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

                        if (cConfiguration.moveToFirst()) {lñ

                            do {

                                if (cConfiguration.getString(cConfiguration.getColumnIndex("Code")).equalsIgnoreCase("com.android.mms")){
                                    data.add(new Item(info.loadLabel(packageManager).toString(), info.loadIcon(packageManager)));
                                }

                            } while(cConfiguration.moveToNext());

                        }

                        cConfiguration.close();
                        dbConfiguration.close();

                    } catch (Exception e) {}*//*


                    if ( true ){
                        if (info.packageName.equals("com.android.mms")){
                            data.add(new Item(info.loadLabel(packageManager).toString(), info.loadIcon(packageManager)));
                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applist;
    }*/

}
