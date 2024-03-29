package com.idslatam.solmar.View;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Entities.Menu;
import com.idslatam.solmar.Models.Crud.MenuCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.Fragments.HomeFragment;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import com.idslatam.solmar.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MenuPrincipal extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
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

            Fragment fragment = null;
            Class fragmentClass;

            fragmentClass = HomeFragment.class;


            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

            sendAsistencia();
        }else{
            //generarMenu(savedInstanceState);

            Fragment fragment = null;
            Class fragmentClass;

                    fragmentClass = HomeFragment.class;


            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        }

    }

    public void load(Bundle savedInstanceState){

        String URL = URL_API.concat("Aplicacion/GetAppByUser?id="+DispositivoId);


        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(MenuPrincipal.this);
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

                        if(response!=null){

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

                                    String []valores = new String[7];
                                    int []val = new int[7];

                                    try {
                                        for (int i = 0; i < jsonA.length(); i++) {

                                            JSONObject c = jsonA.getJSONObject(i);
                                            val[i] = c.getInt("Id");
                                            valores[i] = c.getString("Configuracion");

                                            MenuCrud menuCrud = new MenuCrud(mContext);

                                            com.idslatam.solmar.Models.Entities.Menu menu = new Menu();
                                            menu.Nombre = c.getString("Nombre");
                                            menu.MenuId = _Menu_Id;
                                            _Menu_Id = menuCrud.insert(menu);

                                            Log.e("-- Nombre -- ", c.getString("Nombre"));

                                            JSONArray jsonValores = new JSONArray(valores[i]);

                                            for (int j = 0; j < jsonValores.length(); j++) {
                                                JSONObject v = jsonValores.getJSONObject(j);


                                                if(c.getInt("Id")==2){

                                                    if(v.getInt("ConfiguracionId")==3){

                                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + v.getInt("Valor") + "'");
                                                        db.close();

                                                        Log.e("--"+c.getString("Nombre IM ")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                                                    }

                                                    if(v.getInt("ConfiguracionId")==4){

                                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + v.getInt("Valor")  + "'");
                                                        db.close();
                                                        Log.e("--"+c.getString("Nombre IMT")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
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

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } else {
                                Toast.makeText(mContext, "Error de Servidor", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(mContext, "Error de Conexión", Toast.LENGTH_LONG).show();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();

        //noinspection SimplifiableIfStatement

        /*if (id == R.id.action_call) {
            try {

                startActivity(new Intent(this, ContactosActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }*/

        /*if (id == R.id.action_close) {
            try {
                showDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }*/

        if (id == R.id.action_salir) {
            try {
                showDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        /*if (id == R.id.action_tracking) {
            try {

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, new AdapterTrackingF());
                fragmentTransaction.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_alarmtrack) {
            try {

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, new AdapterAlrmTrackF());
                fragmentTransaction.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }*/

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

                                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                //Toast.makeText(MenuPrincipal.this, "Desbloqueado!", Toast.LENGTH_SHORT).show();

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

                Intent intent = new Intent(MenuPrincipal.this, Login.class);
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


            /*try {
                DBHelper dataBaseHelperB = new DBHelper(this);
                SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                //dbU.execSQL("DELETE FROM sqlite_sequence where name ='Alert'");
                dbU.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'Alert'");
                //dbU.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='Alert'");
                dbU.close();

            } catch (Exception e){}*/

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
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject response) {

                        if(response!=null){
                            Log.e("JsonObject Salida ", response.toString());
                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }
                    }
                });

    }

    //-- METODO QUE ENVIA IMAGEN--------------------------------------------------------------------
    public void uploadImage(){

        ProgressDialog dialog;

        dialog = new ProgressDialog(MenuPrincipal.this);
        dialog.setMessage("Enviando Foto...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NumeroCel, Latitud, Longitud, GuidDipositivo FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                NumeroFile = c.getString(c.getColumnIndex("NumeroCel"));
                LatitudFile = c.getString(c.getColumnIndex("Latitud"));
                LongitudFile = c.getString(c.getColumnIndex("Longitud"));
                DispositivoIdFile = c.getString(c.getColumnIndex("GuidDipositivo"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |EXCEPTION | ", e.getMessage());
        }

        try {

            filePath = fileUri.getPath();

            Log.e(" filePath ", String.valueOf(filePath));

            String filePathAux = filePath; //decodeFile(filePath,660, 880);
            //Log.e(" filePathAux ", String.valueOf(filePathAux));

            String URLB = URL_API.concat("/api/Image/file");

            Ion.with(mContext)
                    .load(URLB)
                    .setMultipartParameter("DispositivoId", DispositivoIdFile)
                    .setMultipartParameter("Latitud", LatitudFile)
                    .setMultipartParameter("Longitud", LongitudFile)
                    .setMultipartParameter("Numero", NumeroFile)

                    .setMultipartFile("file", new File(filePathAux))
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> response) {

                            File fdelete = new File(filePathAux);

                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    Log.e("file Deleted :", filePathAux);
                                } else {
                                    Log.e("file not Deleted :", filePathAux);
                                }
                            }

                            if(response!=null){

                                try {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }catch (Exception e7){}

                                try {
                                    JSONObject json = new JSONObject(response.getResult().toString());

                                    showAlert(json.getString("Mensaje"));

                                } catch (JSONException edd){

                                }

                                Log.e("JsonObject ", response.getResult().toString());
                            } else  {
                                Toast.makeText(mContext, "Error al enviar imagen. Por favor revise su conexión.", Toast.LENGTH_SHORT).show();
                                Log.e("Exception ", "Finaliza "+ e.getMessage());
                            }

                            try {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }catch (Exception ee){}
                        }
                    });


        } catch (Exception e) {

        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Respuesta de Servidor")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*private String decodeFile(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            String s = "SOLMAR_"+timeStamp+".png";

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 20, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }*/

    //**********************************************************************************************

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        Log.e("MENUPRINCIPAL", "RESULT");

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Intent i = new Intent(this, CodeBar.class);
                i.putExtra("epuzzle", result.getContents());
                i.putExtra("format", result.getFormatName());
                startActivity(i);
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        // This is important, otherwise the result will not be passed to the fragment
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Log.e("MENUPRINCIPAL", "REQUEST_CAMERA");
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            //Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), mSize.x, mSize.x);
            //((ImageView) findViewById(R.id.image)).setImageBitmap(bitmap);
            Log.e("MENUPRINCIPAL", "REQUEST_CAMERA FIN " + String.valueOf(photoUri));
            fileUri = photoUri;

            uploadImage();

        }

        Log.e("MENUPRINCIPAL", "RESULT FIN");

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void sendAsistencia(){

        String Num = null, DisId = null;
        String FechaInicioCelular = formatoGuardar.format(new Date());

        try {

            FechaInicioCelular = formatoGuardar.format(new Date());
            DBHelper dbhGUID = new DBHelper(mContext);
            SQLiteDatabase dbA = dbhGUID.getReadableDatabase();
            String selectQueryA = "SELECT NumeroCel, GuidDipositivo FROM Configuration";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToFirst()) {

                Num = cA.getString(cA.getColumnIndex("NumeroCel"));
                DisId = cA.getString(cA.getColumnIndex("GuidDipositivo"));

            }
            cA.close();
            dbA.close();

        }catch (Exception e){}

        String URL = URL_API.concat("api/Attendance");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", Num);
        json.addProperty("DispositivoId", DisId);
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

                            String AsistenciaId = null;
                            //Log.e("JsonObject ", response.toString());
                            AsistenciaId = response.get("AsistenciaId").getAsString();
                            //AsistenciaId
                            //String AsistenciaId = json.get("AsistenciaId").getAsString();

                            try {

                                DBHelper dataBaseHelper = new DBHelper(mContext);
                                SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                                dbT.execSQL("UPDATE Configuration SET AsistenciaId = '"+response.get("AsistenciaId").getAsString()+"'");
                                dbT.close();

                            } catch (Exception vdse){}
                            Log.e("JsonObject F ", response.toString());

                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }
                    }
                });
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new android.support.v7.app.AlertDialog.Builder(MenuPrincipal.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MenuPrincipal.this.requestForPermission(permission);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(MenuPrincipal.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    public void requestForCameraPermission() {
        Log.e("MENUPRINCIPAL", "requestForCameraPermission");

        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(MenuPrincipal.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MenuPrincipal.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
                Log.e("MENUPRINCIPAL", "ActivityCompat");
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
            Log.e("MENUPRINCIPAL ", "launch");
        }

        Log.e("MENUPRINCIPAL", "requestForCameraPermission FIN");
    }

    private void launch() {
        Log.e("launch","launch.....");
        //Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        //startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);

        Log.e("launch","launch FIN.....");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launch();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = null;
        Class fragmentClass;

        fragmentClass = HomeFragment.class;


        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        android.support.v4.app.FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

    public void clickSetting(View view){

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

                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        //Toast.makeText(MenuPrincipal.this, "Desbloqueado!", Toast.LENGTH_SHORT).show();

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

    }

}