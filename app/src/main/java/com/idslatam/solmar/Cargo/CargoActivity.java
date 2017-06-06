package com.idslatam.solmar.Cargo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.desmond.squarecamera.ImageUtility;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.Perfil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CargoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private Toolbar toolbar;
    ViewPager viewPager;

    String GuidDipositivo, URL_API, valor, formato;

    Context mContext;
    boolean btn_dni = false;

    EditText primero_edt_tracto, primero_edt_dni;

    EditText segundo_edt_or, segundo_edt_cta_bultos;

    Calendar currenCodeBar;

    TextView primero_txt_mje;

    TextView segundo_txt_ingreso_tracto, segundo_txt_carga, segundo_txt_dni;

    TextView tercer_txt_ingreso_tracto, tercer_txt_carga, tercer_txt_dni;

    RadioGroup radiogroup;

    RadioButton radio_sinCarga, radio_cargaSuelta, radio_vacio, radio_lleno;

    ImageView imgEstadoDelantera, imgEstadoTrasera, imgEstadoPaniramica;

    CheckBox check_casco, check_chaleco, check_botas, check_carga;

    Switch isLicencia;


    boolean fotoDelantera = false, fotoTracera = false, fotoPanoramica = false;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    String Uri_Foto, idRadioButtom;

    Uri photoUri;

    int _Cargo_Id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargo);

        /*toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);*/

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //1qLog.e("P ", String.valueOf(position));

        if (position == 0) {
            //set values to EditTexts to pull data from 3 entry fragments and display in SwipeReviewResults
            primero_edt_tracto = (EditText) viewPager.findViewById(R.id.primero_edt_tracto);
            primero_edt_dni = (EditText) viewPager.findViewById(R.id.primero_edt_dni);
            primero_txt_mje = (TextView)viewPager.findViewById(R.id.primero_txt_mje);

            radio_sinCarga = (RadioButton) viewPager.findViewById(R.id.radio_sinCarga);
            radio_cargaSuelta = (RadioButton) viewPager.findViewById(R.id.radio_cargaSuelta);
            radio_vacio = (RadioButton) viewPager.findViewById(R.id.radio_vacio);
            radio_lleno = (RadioButton) viewPager.findViewById(R.id.radio_lleno);

            check_casco = (CheckBox) viewPager.findViewById(R.id.check_casco);
            check_chaleco = (CheckBox) viewPager.findViewById(R.id.check_chaleco);
            check_botas = (CheckBox) viewPager.findViewById(R.id.check_botas);

            isLicencia = (Switch)findViewById(R.id.switch_licencia);

            isLicencia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked){

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET isLicencia = 'false'");
                            dba.close();
                            Log.e("isChecked ","false");
                        } catch (Exception eew){}

                    } else {
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET isLicencia = 'true'");
                            dba.close();
                            Log.e("isChecked ","true");
                        } catch (Exception eew){}
                    }
                    //Toast.makeText(ScanCode.this, "Is checked? "+swCarga.isChecked(), Toast.LENGTH_SHORT).show();

                }
            });

            radiogroup =  (RadioGroup) findViewById(R.id.opciones_carga);
            Button bt = (Button) findViewById(R.id.primero_btn_verificar);

            bt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String Placa = null, Dni = null;

                    try {
                        DBHelper dataBaseHelper = new DBHelper(mContext);
                        SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                        String selectQuery = "SELECT Placa, Dni FROM Cargo";

                        Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                        if (c.moveToFirst()) {
                            Placa = c.getString(c.getColumnIndex("Placa"));
                            Dni = c.getString(c.getColumnIndex("Dni"));

                        }
                        c.close();
                        dbst.close();

                    } catch (Exception e) {}

                    if (Placa == null){
                        Toast.makeText(mContext, "Buscar Placa ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Dni == null){
                        Toast.makeText(mContext, "Buscar DNI", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // get selected radio button from radioGroup
                    int selectedId = radiogroup.getCheckedRadioButtonId();
                    // find the radio button by returned id
                    RadioButton radioButton = (RadioButton) findViewById(selectedId);

                    if (radioButton.getText().toString().equalsIgnoreCase("Sin Carga")){
                        idRadioButtom = "1";
                    } else if ((radioButton.getText().toString().equalsIgnoreCase("Carga Suelta"))){
                        idRadioButtom = "2";
                    } else if ((radioButton.getText().toString().equalsIgnoreCase("Contenedor Vacío"))){
                        idRadioButtom = "3";
                    } else if ((radioButton.getText().toString().equalsIgnoreCase("Contenedor Lleno"))){
                        idRadioButtom = "4";
                    }

                    Log.e("idRadioButtom ", idRadioButtom);

                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

                    try {
                        DBHelper dbHelperNumero = new DBHelper(mContext);
                        SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                        dbNro.execSQL("UPDATE Cargo SET TipoCarga = '"+idRadioButtom+"' WHERE CargoId = 1");
                        dbNro.close();

                        Log.e("TipoCarga ","true");
                    } catch (Exception eew){
                        Log.e("Exception ", "TipoCarga");
                    }

                    //Toast.makeText(CargoActivity.this, radioButton.getText(), Toast.LENGTH_SHORT).show();
                }
            });

            poblarPrimeraVista();


        }

        if (position == 1){

            segundo_edt_or = (EditText) viewPager.findViewById(R.id.segundo_edt_or);
            segundo_edt_cta_bultos = (EditText) viewPager.findViewById(R.id.segundo_edt_cta_bultos);

            segundo_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.segundo_txt_ingreso_tracto);
            segundo_txt_carga = (TextView) viewPager.findViewById(R.id.segundo_txt_carga);
            segundo_txt_dni = (TextView) viewPager.findViewById(R.id.segundo_txt_dni);

            check_carga = (CheckBox) viewPager.findViewById(R.id.sengundo_check_carga);

            poblarSegundaVista();

        }

        if (position == 2){

            tercer_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.tercero_txt_ingreso_tracto);
            tercer_txt_carga = (TextView) viewPager.findViewById(R.id.tercero_txt_carga);
            tercer_txt_dni = (TextView) viewPager.findViewById(R.id.tercero_txt_dni);

            imgEstadoDelantera = (ImageView) viewPager.findViewById(R.id.ic_estado_delantera);
            imgEstadoTrasera = (ImageView) viewPager.findViewById(R.id.ic_estado_trasera);
            imgEstadoPaniramica = (ImageView) viewPager.findViewById(R.id.ic_estado_panoramica);

            poblarTerceraVista();

        }

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("POSITION ", String.valueOf(position));

        String Placa = null, Dni = null, NroOR = null, CantidadBultos = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, Dni, NroOR, CantidadBultos FROM Cargo";

            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                NroOR = c.getString(c.getColumnIndex("NroOR"));
                CantidadBultos = c.getString(c.getColumnIndex("CantidadBultos"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (position == 1 && Placa == null){

            Toast.makeText(mContext, "Buscar Placa ", Toast.LENGTH_SHORT).show();

            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","0");

            return;
        }

        if (position == 1 && Dni == null){

            Toast.makeText(mContext, "Buscar DNI", Toast.LENGTH_SHORT).show();

            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","0");

            return;
        }

        if (position == 2 && NroOR == null){
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","1");

            Toast.makeText(mContext, "Ingrese OR/GR", Toast.LENGTH_SHORT).show();

            return;
        }

        if (position == 2 && CantidadBultos == null){
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","1");
            Toast.makeText(mContext, "Ingrese CantidadBultos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position == 0){

            primero_edt_tracto = (EditText) viewPager.findViewById(R.id.primero_edt_tracto);
            primero_edt_dni = (EditText) viewPager.findViewById(R.id.primero_edt_dni);
            primero_txt_mje = (TextView)viewPager.findViewById(R.id.primero_txt_mje);

            radio_sinCarga = (RadioButton) viewPager.findViewById(R.id.radio_sinCarga);
            radio_cargaSuelta = (RadioButton) viewPager.findViewById(R.id.radio_cargaSuelta);
            radio_vacio = (RadioButton) viewPager.findViewById(R.id.radio_vacio);
            radio_lleno = (RadioButton) viewPager.findViewById(R.id.radio_lleno);

            check_casco = (CheckBox) viewPager.findViewById(R.id.check_casco);
            check_chaleco = (CheckBox) viewPager.findViewById(R.id.check_chaleco);
            check_botas = (CheckBox) viewPager.findViewById(R.id.check_botas);

            isLicencia = (Switch)findViewById(R.id.switch_licencia);

        }

        if (position == 1){

            segundo_edt_or = (EditText) viewPager.findViewById(R.id.segundo_edt_or);
            segundo_edt_cta_bultos = (EditText) viewPager.findViewById(R.id.segundo_edt_cta_bultos);

            segundo_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.segundo_txt_ingreso_tracto);
            segundo_txt_carga = (TextView) viewPager.findViewById(R.id.segundo_txt_carga);
            segundo_txt_dni = (TextView) viewPager.findViewById(R.id.segundo_txt_dni);

            check_carga = (CheckBox) viewPager.findViewById(R.id.sengundo_check_carga);

        }

        if (position == 2){

            tercer_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.tercero_txt_ingreso_tracto);
            tercer_txt_carga = (TextView) viewPager.findViewById(R.id.tercero_txt_carga);
            tercer_txt_dni = (TextView) viewPager.findViewById(R.id.tercero_txt_dni);

            imgEstadoDelantera = (ImageView) viewPager.findViewById(R.id.ic_estado_delantera);
            imgEstadoTrasera = (ImageView) viewPager.findViewById(R.id.ic_estado_trasera);
            imgEstadoPaniramica = (ImageView) viewPager.findViewById(R.id.ic_estado_panoramica);

        }

    }

    public void onCheckboxClickedSegundo(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.sengundo_check_carga:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET isCarga = 'true'");
                    db.close();
                    Log.e("isCarga ","true");
                }else {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET isCarga = 'false'");
                    db.close();
                    Log.e("isCarga ","isCarga");
                }
                break;
        }
    }

    public void segundo_btn_fotos(View view){
        if (segundo_edt_or.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese OR/GR", Toast.LENGTH_SHORT).show();
            return;
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET NroOR = "+segundo_edt_or.getText().toString()+"");
            db.close();
            Log.e("segundo_btn_fotos ","isCarga");
        }

        if (segundo_edt_cta_bultos.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese cantidad de bultos", Toast.LENGTH_SHORT).show();
            return;

        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET CantidadBultos = "+segundo_edt_cta_bultos.getText().toString()+"");
            db.close();
            Log.e("segundo_edt_cta_bultos ","CantidadBultos");
        }

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.check_casco:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppCasco = 'true'");
                    db.close();
                    Log.e("check_casco ","true");
                }else {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppCasco = 'false'");
                    db.close();
                    Log.e("check_casco ","false");
                }

                break;
            case R.id.check_chaleco:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppChaleco = 'true'");
                    db.close();
                    Log.e("check_chaleco ","true");
                }else{
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppChaleco = 'false'");
                    db.close();
                    Log.e("check_chaleco ","false");
                }

            case R.id.check_botas:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppBotas = 'true'");
                    db.close();
                    Log.e("check_botas ","true");
                }else{
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppBotas = 'false'");
                    db.close();
                    Log.e("check_botas ","false");
                }
                break;
        }


    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public void scanDNI(View view){

        btn_dni = true;
        scanBarcode();
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    public void enviarPlaca(View view) {

        String pla = null;

        if (primero_edt_tracto.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese PLaca", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            DBHelper dbHelperNumero = new DBHelper(this);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            dbNro.execSQL("UPDATE Cargo SET Placa = '"+primero_edt_tracto.getText().toString()+"' WHERE CargoId = 1");
            dbNro.close();

            Log.e("SET Placa ", "true "+primero_edt_tracto.getText().toString() );

        } catch (Exception eew){
            Log.e("Exception Placa ", "");
        }

        //Toast.makeText(this, " Enviar Placa "+ primero_edt_tracto.getText().toString(), Toast.LENGTH_SHORT).show();

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                GuidDipositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));

            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        //Log.e("DispositivoId ", GuidDipositivo);

        pla = primero_edt_tracto.getText().toString();

        if (true){

            String URL = URL_API.concat("api/Cargo/VerificaPlaca?Placa="+primero_edt_tracto.getText().toString()+"&DispositivoId="+GuidDipositivo+"");



            ProgressDialog pDialog;

            pDialog = new ProgressDialog(CargoActivity.this);
            pDialog.setMessage("Verificando Placa...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();


            String finalPla = pla;
            Ion.with(this)
                    .load("GET", URL)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> response) {

                            if(response == null){

                                Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

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

                                Log.e("JsonObject PLACA ", result.toString());


                                if (!result.get("CargoTipoMovimiento").isJsonNull()){

                                    if (result.get("CargoTipoMovimiento").getAsString().equalsIgnoreCase("1")){
                                        try {
                                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                            dba.execSQL("UPDATE Cargo SET isIngreso = 'true'");
                                            dba.close();
                                        } catch (Exception eew){
                                            Log.e("Exceptoion isIngreso ", "");
                                        }

                                        //
                                        primero_txt_mje.setText("El Tracto "+primero_edt_tracto.getText().toString()+" está Ingresando");
                                    } else {
                                        try {
                                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                            dba.execSQL("UPDATE Cargo SET isIngreso = 'false'");
                                            dba.close();
                                        } catch (Exception eew){
                                            Log.e("Exceptoion isIngreso ", "");
                                        }
                                        primero_txt_mje.setText("El Tracto "+primero_edt_tracto.getText().toString()+" está Saliendo");
                                    }

                                }

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }

                                } catch (Exception edd){

                                }

                            } else  {

                                Toast.makeText(CargoActivity.this, "Error. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();
                                Log.e("ExceptionV ", "Finaliza" );
                            }

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        }
                    });

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            btn_dni = false;
            //finish();
            return;
        }

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                if (btn_dni){
                    primero_edt_dni.setText(result.getContents());

                    valor = result.getContents();
                    formato = result.getFormatName();
                    enviarDNI();
                }

                Log.e("Cargo Activity ", result.getFormatName());
            }
        }

        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_CAMERA) {
            photoUri = data.getData();

            Uri_Foto = photoUri.getPath();

            if (fotoDelantera){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoDelantera = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoDelantera ","true");
                } catch (Exception eew){
                    Log.e("Exception ", "fotoDelantera");
                }

                imgEstadoDelantera.setImageResource(R.drawable.ic_estado_ok);

                fotoDelantera = false;


            } else if (fotoTracera){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoTracera = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoTracera ","true");
                } catch (Exception eew){
                    Log.e("Exception ", "fotoTracera");
                }

                imgEstadoTrasera.setImageResource(R.drawable.ic_estado_ok);

                fotoTracera = false;

            } else {

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoPanoramica = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoPanoramica ","true");
                } catch (Exception eew){
                    Log.e("Exception ", "fotoPanoramica");
                }

                imgEstadoPaniramica.setImageResource(R.drawable.ic_estado_ok);

                fotoPanoramica = false;

            }

            Log.e(" Position GUID ", String.valueOf(photoUri.getPath()));
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void enviarDNI(){

        String NumeroCel = null, CodigoEmpleado = null;

        currenCodeBar = Calendar.getInstance();
        String fecha = formatoGuardar.format(currenCodeBar.getTime());
        String tipo = "1";

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

        if (formato==null){
            formato = "No Scan";
        }

        Log.e("---! Send: TIPO "+tipo, " ! VALOR "+valor + " ! FECHA "+fecha+" ! FORMATO "+formato);


        if (true){

            String URL = URL_API.concat("api/CodigoBarra");


            ProgressDialog pDialog;

            pDialog = new ProgressDialog(CargoActivity.this);
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

                            try {
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE Cargo SET Dni = "+primero_edt_dni.getText().toString()+"");
                                dba.close();
                                Log.e("Dni ","true");
                            } catch (Exception eew){
                                Log.e("Exception ", "Dni");
                            }


                            if(response == null){

                                Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

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

                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception ecsa){}

                            } else  {

                                Log.e("Error Code DNI ", String.valueOf(response.getHeaders().code()));

                                Toast.makeText(CargoActivity.this, "Error Code " +  response.getHeaders().code(), Toast.LENGTH_LONG).show();
                                Log.e("Exception ", "Finaliza" );
                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception ecsa){}
                            }

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception ecsa){}
                        }
                    });
        }

    }

    public void dialogoRespuesta(String Estado, String Header, String Mensaje){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);

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

    public void finalizarSend(View view){

        String Numero = null, DispositivoId = null, Placa = null, TipoCarga = null, Casco = null, Chaleco = null, Botas = null,
                Dni = null, Licencia = null, NroOR = null, Carga = null, Delantera = null, Trasera = null,
                Panoramica = null, CargoTipoMovimiento = null, CantidadBultos = null;


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT GuidDipositivo, NumeroCel FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Numero = c.getString(c.getColumnIndex("NumeroCel"));
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, EppCasco, EppChaleco, EppBotas," +
                    "Dni, isLicencia, NroOR, isCarga, CantidadBultos, isIngreso, fotoDelantera, fotoTracera, fotoPanoramica FROM Cargo";

            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Casco = c.getString(c.getColumnIndex("EppCasco"));
                Chaleco = c.getString(c.getColumnIndex("EppChaleco"));
                Botas = c.getString(c.getColumnIndex("EppBotas"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                Licencia = c.getString(c.getColumnIndex("isLicencia"));
                NroOR = c.getString(c.getColumnIndex("NroOR"));
                Carga = c.getString(c.getColumnIndex("isCarga"));
                Delantera = c.getString(c.getColumnIndex("fotoDelantera"));
                Trasera = c.getString(c.getColumnIndex("fotoTracera"));
                Panoramica = c.getString(c.getColumnIndex("fotoPanoramica"));
                CargoTipoMovimiento = c.getString(c.getColumnIndex("isIngreso"));
                CantidadBultos = c.getString(c.getColumnIndex("CantidadBultos"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (CargoTipoMovimiento.equalsIgnoreCase("true")){
            CargoTipoMovimiento = "1";

        } else {
            CargoTipoMovimiento = "2";
        }

        if (Placa==null){
            Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Dni==null){
            Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (NroOR==null){
            Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CantidadBultos==null){
            Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Delantera==null){
            Toast.makeText(mContext, "Falta Foto Delantera", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Trasera==null){
            Toast.makeText(mContext, "Falta Foto Trasera", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Panoramica==null){
            Toast.makeText(mContext, "Falta Foto Panorámica", Toast.LENGTH_SHORT).show();
            return;
        }


        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/CreateCargaSuelta");

        Log.e("Numero ", Numero);
        Log.e("DispositivoId ", DispositivoId);
        Log.e("Placa ", Placa);
        Log.e("CargoTipoMovimientoId ", CargoTipoMovimiento);
        Log.e("CargoTipoCargaId ", TipoCarga);
        Log.e("Casco ", Casco);
        Log.e("Chaleco ", Chaleco);
        Log.e("Botas ", Botas);
        Log.e("VigenciaLicencia ", Licencia);
        Log.e("NroDOI", Dni);
        Log.e("NroORGR ", NroOR);
        //Log.e("Carga ", Carga);
        Log.e("CantidadBultos ", CantidadBultos);
        Log.e("Delantera ", Delantera);
        Log.e("Trasera ", Trasera);
        Log.e("Panoramica ", Panoramica);

        Ion.with(mContext)
                .load(URL)
                .setMultipartParameter("Numero", Numero)
                .setMultipartParameter("DispositivoId", DispositivoId)
                .setMultipartParameter("Placa", Placa)
                .setMultipartParameter("CargoTipoMovimientoId", CargoTipoMovimiento)
                .setMultipartParameter("CargoTipoCargaId", TipoCarga)
                .setMultipartParameter("Casco", Casco)
                .setMultipartParameter("Chaleco", Chaleco)
                .setMultipartParameter("Botas", Botas)
                .setMultipartParameter("VigenciaLicenciaConducir", Licencia)
                .setMultipartParameter("NroDOI", Dni)
                .setMultipartParameter("NroORGR", NroOR)
                .setMultipartParameter("CantidadBultos", CantidadBultos)
                //.setMultipartParameter("Carga", Carga)
                .setMultipartFile("Delantera", new File(Delantera))
                .setMultipartFile("Trasera", new File(Trasera))
                .setMultipartFile("Panoramica", new File(Panoramica))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        /*
                        Gson gsona = new Gson();
                        JsonObject resulta = gsona.fromJson(response.getResult(), JsonObject.class);

                        Log.e("JsonObjecta ", resulta.toString());
                        */

                        /*if (e!=null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                            Log.e("Exception ", "Exception");
                            return;
                        }*/

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            try {

                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE Cargo SET fotoDelantera = " + null);
                                dba.execSQL("UPDATE Cargo SET fotoTracera = " + null);
                                dba.execSQL("UPDATE Cargo SET fotoPanoramica = " + null);
                                dba.execSQL("UPDATE Cargo SET Placa = " + null);
                                dba.execSQL("UPDATE Cargo SET Dni = " + null);
                                dba.execSQL("UPDATE Cargo SET NroOR = " + null);

                                dba.close();

                            } catch (Exception eew){}


                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                        try {
                            showDialogSend();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    public void poblarPrimeraVista(){

        Log.e("poblarPrimeraVista ", "Ingreso");

        String Placa = null, TipoCarga = null, Dni = null, isCarga = null, isIngresoS = null,
                EppCasco = null, EppChaleco = null, EppBotas = null, isLicenciaL = null;
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, EppCasco, EppChaleco, " +
                    "EppBotas, isLicencia FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngresoS = c.getString(c.getColumnIndex("isIngreso"));
                EppCasco = c.getString(c.getColumnIndex("EppCasco"));
                EppChaleco = c.getString(c.getColumnIndex("EppChaleco"));
                EppBotas = c.getString(c.getColumnIndex("EppBotas"));
                isLicenciaL = c.getString(c.getColumnIndex("isLicencia"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        //Placa = "A";

        if (Placa==null){
            return;
        }
        primero_edt_tracto.setText(Placa);

        if (Dni==null){
            return;
        }
        primero_edt_dni.setText(Dni);

        if (isIngresoS==null){
            return;
        }

        if (isIngresoS.equalsIgnoreCase("true")){
            primero_txt_mje.setText("El Tracto "+Placa+" está Ingresando");
        } else {
            primero_txt_mje.setText("El Tracto "+Placa+" está Saliendo");
        }

        if (TipoCarga.equalsIgnoreCase("1")){ radio_sinCarga.setChecked(true);
        } else if (TipoCarga.equalsIgnoreCase("2")){ radio_cargaSuelta.setChecked(true);
        } else if (TipoCarga.equalsIgnoreCase("3")){ radio_vacio.setChecked(true);
        } else if (TipoCarga.equalsIgnoreCase("4")){ radio_lleno.setChecked(true); }

        if (EppCasco.equalsIgnoreCase("true")){ check_casco.setChecked(true);}

        if (EppChaleco.equalsIgnoreCase("true")){ check_chaleco.setChecked(true);}

        if (EppBotas.equalsIgnoreCase("true")){ check_botas.setChecked(true);}

        if (isLicenciaL.equalsIgnoreCase("false")){ isLicencia.setChecked(true);}

    }

    public void buscarDNI(View view){

        if (primero_edt_dni.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese DNI", Toast.LENGTH_SHORT).show();
            return;
        }

        valor = primero_edt_dni.getText().toString();
        enviarDNI();

    }

    public void poblarSegundaVista(){

        String Placa = null, TipoCarga = null, Dni = null, isCarga = null, isIngreso = null,
        or = null, ctaBultos = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, isCarga, NroOR, CantidadBultos FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));
                isCarga = c.getString(c.getColumnIndex("isCarga"));
                or = c.getString(c.getColumnIndex("NroOR"));
                ctaBultos = c.getString(c.getColumnIndex("CantidadBultos"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){
            return;
        }

        Log.e("Placa ", Placa);

        if (isIngreso.equalsIgnoreCase("true")){
            segundo_txt_ingreso_tracto.setText("Ingreso de Tracto "+Placa);
        } else {
            segundo_txt_ingreso_tracto.setText("Salida de Tracto "+Placa);
        }

        if (TipoCarga.equalsIgnoreCase("1")){
            segundo_txt_carga.setText("Sin Carga");
        } else if (TipoCarga.equalsIgnoreCase("2")){
            segundo_txt_carga.setText("Carga Suelta");
        } else if (TipoCarga.equalsIgnoreCase("3")){
            segundo_txt_carga.setText("Contenedor Vacío");
        }else {
            segundo_txt_carga.setText("Contenedor LLeno");
        }
        segundo_txt_dni.setText("Conductor con DNI "+ Dni + "");

        if (isCarga.equalsIgnoreCase("true")){
            check_carga.setChecked(true);
        }

        if (or == null){
            return;
        }
        segundo_edt_or.setText(or);

        if (ctaBultos == null){
            return;
        }

        segundo_edt_cta_bultos.setText(ctaBultos);

    }

    public void poblarTerceraVista(){

        String Placa = null, TipoCarga = null,
                Dni = null, isCarga = null, isIngreso = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){
            return;
        }

        Log.e("Placa ", Placa);

        if (isIngreso.equalsIgnoreCase("true")){
            tercer_txt_ingreso_tracto.setText("Ingreso de Tracto "+Placa);
        } else {
            tercer_txt_ingreso_tracto.setText("Salida de Tracto "+Placa);
        }

        if (TipoCarga.equalsIgnoreCase("1")){
            tercer_txt_carga.setText("Sin Carga");
        } else if (TipoCarga.equalsIgnoreCase("2")){
            tercer_txt_carga.setText("Carga Suelta");
        } else if (TipoCarga.equalsIgnoreCase("3")){
            tercer_txt_carga.setText("Contenedor Vacío");
        }else {
            tercer_txt_carga.setText("Contenedor LLeno");
        }

        tercer_txt_dni.setText("Conductor con DNI "+ Dni + "");

    }

    public void parteDelantera(View view){
        fotoDelantera = true;
        fotoTracera = false;
        fotoPanoramica = false;

        tomarFoto();
    }

    public void parteTracera(View view){
        fotoDelantera = false;
        fotoTracera = true;
        fotoPanoramica = false;

        tomarFoto();
    }

    public void partePanoramica(View view){
        fotoDelantera = false;
        fotoTracera = false;
        fotoPanoramica = true;

        tomarFoto();
    }

    // METODOS DE CAMARA
    public void tomarFoto(){

        photoUri = null;

        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(CargoActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CargoActivity.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
        }
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new android.support.v7.app.AlertDialog.Builder(CargoActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CargoActivity.this.requestForPermission(permission);
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
        ActivityCompat.requestPermissions(CargoActivity.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launch() {
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void returnPersona(View view){
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public void returnCarga(View view){
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

}
