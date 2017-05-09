package com.idslatam.solmar.View;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.gson.JsonObject;
import com.idslatam.solmar.Alert.AlertActivity;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Dialer.ContactosActivity;
import com.idslatam.solmar.ImageClass.Image;
import com.idslatam.solmar.Models.Crud.MenuCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
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

import static android.R.attr.fragment;

public class Perfil extends AppCompatActivity implements AdapterView.OnItemClickListener {

    protected String URL_API;
    private Toolbar toolbar;
    String fotocheckCod;
    Bundle b;
    Context mContext;

    private Uri fileUri;
    private String filePath = null;
    int _Menu_Id = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String DispositivoIdFile, LatitudFile, LongitudFile, NumeroFile, DispositivoId;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    boolean state;


    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Point mSize;

    boolean isFragment = false;

    GridView gridview;
    GridViewAdapter gridviewAdapter;
    ArrayList<Item> data = new ArrayList<Item>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        toolbar = (Toolbar) findViewById(R.id.app_bar);

        mContext = this;

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT CodigoEmpleado, GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                fotocheckCod = cConfiguration.getString(cConfiguration.getColumnIndex("CodigoEmpleado"));
                DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
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

        if (state == true) {
            load(savedInstanceState);
            //sendAsistencia();
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

                    } while(cConfiguration.moveToNext());

                }

            cConfiguration.close();
            dbConfiguration.close();

            //data.add(new Item("Bars", getResources().getDrawable(R.mipmap.ic_barsa)));
            data.add(new Item("Llamadas", getResources().getDrawable(R.mipmap.ic_llamada)));
            data.add(new Item("Mensajes", getResources().getDrawable(R.mipmap.ic_mje)));
            data.add(new Item("Configuración", getResources().getDrawable(R.mipmap.ic_settings)));

        } catch (Exception e) {}

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
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            startActivity(intent);

                            return;
                        }

                            if (response.getHeaders().code() == 200) {

                                try {

                                    JSONObject json = new JSONObject(response.getResult().toString());
                                    Log.e("JSON GET: Boolean", json.toString());

                                    String Menu = null;
                                    try {
                                        Menu = json.getString("Menu");
                                    } catch (JSONException e2) {
                                        e.printStackTrace();
                                    }

                                    JSONArray jsonA = null;

                                    try {
                                        jsonA = new JSONArray(Menu);
                                    } catch (JSONException e3) {
                                        e.printStackTrace();
                                    }

                                    String []valores = new String[5];
                                    int []val = new int[5];

                                    try {
                                        for (int i = 0; i < jsonA.length(); i++) {

                                            JSONObject c = jsonA.getJSONObject(i);
                                            val[i] = c.getInt("Id");
                                            valores[i] = c.getString("Configuracion");

                                            MenuCrud menuCrud = new MenuCrud(mContext);

                                            com.idslatam.solmar.Models.Entities.Menu menu = new com.idslatam.solmar.Models.Entities.Menu();
                                            menu.Nombre = c.getString("Nombre");
                                            menu.Code = c.getString("Id");
                                            menu.MenuId = _Menu_Id;
                                            _Menu_Id = menuCrud.insert(menu);


                                            Log.e("-- Nombre -- ", c.getString("Nombre"));
                                            Log.e("-- Id -- ", c.getString("Id"));

                                            JSONArray jsonValores = new JSONArray(valores[i]);

                                            for (int j = 0; j < jsonValores.length(); j++) {
                                                JSONObject v = jsonValores.getJSONObject(j);


                                                // ALERT
                                                if(c.getInt("Id")==2){

                                                    if(v.getInt("ConfiguracionId")==3){

                                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + v.getInt("Valor") + "'");
                                                        db.close();

                                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                                                    }

                                                    if(v.getInt("ConfiguracionId")==4){

                                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + v.getInt("Valor")  + "'");
                                                        db.close();
                                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                                                    }
                                                }

                                                // BRAVO PAPA
                                                if(c.getInt("Id")==3){

                                                    if(v.getInt("ConfiguracionId")==5){

                                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                        db.execSQL("UPDATE Configuration SET VecesPresionarVolumen = '"+v.getInt("Valor")+"'");
                                                        db.close();

                                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e6){
                                        Log.e("-- Error! ", " al obtener Menu");
                                    }

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

                                Log.e("-- Get Menu! ", " Finaliza");

                                //generarMenu(savedInstanceState);
                                initView(); // Initialize the GUI Components
                                fillData(); // Insert The Data
                                setDataAdapter(); // Set the Data Adapter

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }


                            } else {
                                Intent intent = new Intent(Perfil.this, Login.class);
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                startActivity(intent);

                                Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
                            }

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

            dialog = new ProgressDialog(Perfil.this);
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
        protected void onPostExecute(Void result)
        {
            try {

                DBHelper dbgelperDeete = new DBHelper(mContext);
                SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
                sqldbDelete.execSQL("DELETE FROM Menu");
                sqldbDelete.close();

                Intent intent = new Intent(Perfil.this, Login.class);
                startActivity(intent);

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                finish();
            }catch (Exception e){}
        }
    }

    private Boolean finalizarTurno(){

        try {
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

        Log.e("ID "+ String.valueOf(statusBoton) + " | Estado Btn "+ statusBoton, " | Estado Turno "+ statusFinTurno);

        if(statusBoton.equals("false") && statusFinTurno.equals("true")){

            try {
                DBHelper dataBaseHelperB = new DBHelper(this);
                SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                dbU.execSQL("DELETE FROM Alert WHERE AlertId = "+idUp);
                dbU.close();

            } catch (Exception e){}


            Log.e("Fin Consulta ", " Eliminar ");

        }

        cerrarSesion();

        return true;
    }

    public void cerrarSesion(){

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

        String URL = URL_API.concat("Attendance/end");

        JsonObject json = new JsonObject();
        json.addProperty("AsistenciaId", AsistenciaId);
        json.addProperty("DispositivoId", DispositivoId);
        json.addProperty("FechaTerminoCelular", FechaTerminoCelular);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if(response == null){
                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(response.getHeaders().code()==200){
                            Log.e("JsonObject Salida ", response.getResult().toString());
                        }
                    }
                });

    }

}
