package com.idslatam.solmar.Cargo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.Perfil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CargoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private Toolbar toolbar;
    ViewPager viewPager;

    String GuidDipositivo, URL_API, valor, formato;

    Context mContext;
    boolean btn_dni = false;

    EditText primero_edt_tracto, primero_edt_dni;

    EditText segundo_edt_or, segundo_edt_dni;

    Calendar currenCodeBar;

    TextView primero_txt_mje;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm");


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

    /*@Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_salir) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //1qLog.e("P ", String.valueOf(position));

        if (position == 0) {
            //set values to EditTexts to pull data from 3 entry fragments and display in SwipeReviewResults
            primero_edt_tracto = (EditText) viewPager.findViewById(R.id.primero_edt_tracto);
            primero_edt_dni = (EditText) viewPager.findViewById(R.id.primero_edt_dni);

            primero_txt_mje = (TextView)viewPager.findViewById(R.id.primero_txt_mje);

            Switch isIngreso = (Switch)findViewById(R.id.switch_licencia);

            isIngreso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // do something, the isChecked will be
                    // true if the switch is in the On position

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

            RadioGroup radiogroup =  (RadioGroup) findViewById(R.id.opciones_carga);
            Button bt = (Button) findViewById(R.id.segundo_btn_persona);

            bt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // get selected radio button from radioGroup
                    int selectedId = radiogroup .getCheckedRadioButtonId();
                    // find the radio button by returned id
                    RadioButton radioButton = (RadioButton) findViewById(selectedId);

                    Toast.makeText(CargoActivity.this,
                            radioButton.getText(), Toast.LENGTH_SHORT).show();
                }
            });



        }

        if (position == 1){

            segundo_edt_or = (EditText) viewPager.findViewById(R.id.segundo_edt_or);
            segundo_edt_dni = (EditText) viewPager.findViewById(R.id.segundo_edt_dni);

        }

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("POSITION ", String.valueOf(position));

        Log.e("p_primero_edt_tracto ", String.valueOf(primero_edt_tracto.getText().toString()));

        if (position == 1 && primero_edt_tracto.getText().toString().matches("")){

            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","0");

        }

        if (position == 2 && segundo_edt_or.getText().toString().matches("")){

            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            Log.e("BACK ","1");

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
                    Log.e("check_casco ","true");
                }else {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET NroOR = 'false'");
                    db.close();
                    Log.e("check_casco ","isCarga");
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
            Log.e("check_casco ","isCarga");
        }

        if (segundo_edt_dni.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese DNI", Toast.LENGTH_SHORT).show();
            return;
        }

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
            // TODO: Veggie sandwich
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
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Cargo SET Placa = "+ primero_edt_tracto.getText().toString());
            dba.close();
        } catch (Exception eew){
            Log.e("Exceptoion Placa ", "");
        }


        Toast.makeText(this, " Enviar Placa "+ primero_edt_tracto.getText().toString(), Toast.LENGTH_SHORT).show();

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

        Log.e("DispositivoId ", GuidDipositivo);

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
                                            Log.e("Exceptoion Placa ", "");
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
                                            Log.e("Exceptoion Placa ", "");
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
                                Log.e("Exception ", "Finaliza" );
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

        Log.e("MENUPRINCIPAL", "RESULT");

        if (resultCode != RESULT_OK){
            btn_dni = false;
            finish();
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
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        // This is important, otherwise the result will not be passed to the fragment

        Log.e("MENUPRINCIPAL", "RESULT FIN");

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
                                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                    dba.execSQL("UPDATE Cargo SET Dni = "+primero_edt_dni.getText().toString()+"");
                                    dba.close();
                                    Log.e("Dni ","true");
                                } catch (Exception eew){
                                    Log.e("Exception ", "Dni");
                                }

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }

                                    dialogoRespuesta(result.get("Estado").getAsString() ,result.get("Header").getAsString(),result.get("Mensaje").getAsString());

                                } catch (Exception edd){

                                }
                                Log.e("JsonObject Bars ", response.toString());

                            } else  {

                                Toast.makeText(CargoActivity.this, "Error al enviar Bars. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();
                                Log.e("Exception ", "Finaliza" );
                            }

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
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

        String Numero = null, Placa = null, TipoCarga = null, Casco = null, Chaleco = null, Botas = null,
                Dni = null, Licencia = null, NroOR = null, Carga = null;


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT EstaActivado, NumeroCel FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Numero = c.getString(c.getColumnIndex("NumeroCel"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, EppCasco, EppChaleco, EppBotas," +
                    "Dni, isLicencia, NroOR, isCarga FROM Cargo";
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
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}



        String URL = URL_API.concat("api/Cargo/Create");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", Numero);
        json.addProperty("Placa", Placa);
        json.addProperty("TipoCarga", TipoCarga);
        json.addProperty("Casco", Casco);
        json.addProperty("Chaleco", Chaleco);
        json.addProperty("Botas", Botas);
        json.addProperty("DNI", Dni);
        json.addProperty("LicenciaActiva", Licencia);
        json.addProperty("NroOR", NroOR);
        json.addProperty("VerificacionCarga", Carga);




        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
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

                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            return;

                        }

                        if (response.getHeaders().code() == 200) {

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }


                        } else {
                            Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
                        }

                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }

                    }
                });


    }

}
