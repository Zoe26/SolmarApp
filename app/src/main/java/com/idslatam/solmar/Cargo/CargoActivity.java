package com.idslatam.solmar.Cargo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.idslatam.solmar.Cargo.Precinto.PrecintoCustomAdapter;
import com.idslatam.solmar.Cargo.Precinto.PrecintoDataModel;
import com.idslatam.solmar.ImageClass.*;
import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Crud.CargoPrecintoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.Perfil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.bitmap.Transform;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CargoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private Toolbar toolbar;
    ViewPager viewPager;

    String GuidDipositivo, URL_API, valor, formato, numeroPrecintoFoto;

    Context mContext;
    boolean btn_dni = false;

    EditText primero_edt_tracto, primero_edt_dni;
    EditText segundo_edt_or, segundo_edt_cta_bultos;
    Calendar currenCodeBar;
    TextView primero_txt_mje, cargo_txt_dni_persona, cargo_txt_empresa_persona;
    TextView segundo_txt_ingreso_tracto, segundo_txt_carga, segundo_txt_dni;
    TextView tercer_txt_ingreso_tracto, tercer_txt_carga, tercer_txt_dni;
    TextView cuarto_txt_ingreso_tracto, cuarto_txt_carga, cuarto_txt_dni;
    TextView quinto_txt_ingreso_tracto, quinto_txt_carga, quinto_txt_dni, quinto_txt_nro_precintos;

    TextView tercero_cargo_text_foto_panoramica;

    ImageView img_cargo_persona; // quinto_btn_precintos,
    EditText cuarto_edt_codContenedor, cuarto_edt_precinto, cuarto_edt_origen, cuarto_edt_or;
    CheckBox check_casco, check_chaleco, check_botas, check_carga;
    SwitchCompat isLicencia, cuarto_switch_tamanoContenedor, cuarto_switch_tipoDoc;
    LinearLayout edt_trasera, edt_panoramica, ly_foto;

    ImageButton btn_visualizar_delantera, btn_visualizar_trasera, btn_visualizar_panoramica;

    boolean fotoDelantera = false, fotoTracera = false, fotoPanoramica = false, isSinCarga, isScanner = false;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");

    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    String Uri_Foto, idRadioButtom, sfotoPanoramica;

    Uri photoUri;

    String Numero = null, DispositivoId = null, Placa = null, TipoCarga = null, Casco = null, Chaleco = null, Botas = null,
            Dni = null, Licencia = null, NroOR = null, Carga = null, Delantera = null, Trasera = null,
            Panoramica = null, CargoTipoMovimiento = null, CantidadBultos = null;

    String sCodigoContenedor, sPrecinto, sNumeroPrecinto, sOrigen, sNumero, stamañoContenedor, sTipoDocumento;

    RadioGroup radiogroup;

    RadioButton rbSinCarga, rbCargaSuelta, rbContenedorLleno, rbContenedorVacio;

    GridView listView;

    ArrayList<PrecintoDataModel> dataModelsMovil;

    PrecintoCustomAdapter adapterMovil;

    int _CargoPrecinto_Id = 0, contadorLista, TIME_OUT = 60 * 1000;

    boolean isPrecinto = false;

    private static final int CAPTURE_MEDIA = 368;

    private Activity activity;

    boolean fotoA,fotoB, fotoC, fotoPrecinto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargo);

        activity = this;
        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.addOnPageChangeListener(this);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        String sTipoCarga = null, isIngresoV = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT TipoCarga, fotoPanoramica, isIngreso FROM Cargo";


            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                sTipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                sfotoPanoramica = c.getString(c.getColumnIndex("fotoPanoramica"));
                isIngresoV = c.getString(c.getColumnIndex("isIngreso"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (position == 0) {

            primero_edt_tracto = (EditText) viewPager.findViewById(R.id.primero_edt_tracto);
            primero_edt_dni = (EditText) viewPager.findViewById(R.id.primero_edt_dni);

            primero_txt_mje = (TextView)viewPager.findViewById(R.id.primero_txt_mje);

            check_casco = (CheckBox) viewPager.findViewById(R.id.check_casco);
            check_chaleco = (CheckBox) viewPager.findViewById(R.id.check_chaleco);
            check_botas = (CheckBox) viewPager.findViewById(R.id.check_botas);

            rbSinCarga = (RadioButton)viewPager.findViewById(R.id.radio_sinCarga);
            rbCargaSuelta = (RadioButton)viewPager.findViewById(R.id.radio_cargaSuelta);
            rbContenedorLleno = (RadioButton)viewPager.findViewById(R.id.radio_lleno);
            rbContenedorVacio = (RadioButton)viewPager.findViewById(R.id.radio_vacio);

            img_cargo_persona = (ImageView)viewPager.findViewById(R.id.img_cargo_persona);
            ly_foto = (LinearLayout) viewPager.findViewById(R.id.ly_foto);
            //ly_foto.setVisibility(View.GONE);

            cargo_txt_dni_persona = (TextView)viewPager.findViewById(R.id.cargo_txt_dni_persona);
            cargo_txt_empresa_persona = (TextView)viewPager.findViewById(R.id.cargo_txt_empresa_persona);


            rbSinCarga.setOnClickListener(rbSinCargaClick);
            rbCargaSuelta.setOnClickListener(rbCargaSueltaClick);
            rbContenedorLleno.setOnClickListener(rbContenedorLlenoClick);
            rbContenedorVacio.setOnClickListener(rbContenedorVacioClick);


            isLicencia = (SwitchCompat)findViewById(R.id.switch_licencia);
            isLicencia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked){

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET isLicencia = 'false'");
                            dba.close();
                        } catch (Exception eew){}

                    } else {
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET isLicencia = 'true'");
                            dba.close();
                        } catch (Exception eew){}
                    }
                    //Toast.makeText(ScanCode.this, "Is checked? "+swCarga.isChecked(), Toast.LENGTH_SHORT).show();

                }
            });

            radiogroup =  (RadioGroup) viewPager.findViewById(R.id.opciones_carga);
            radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    switch(checkedId) {

                        case R.id.radio_sinCarga:

                            try {
                                DBHelper dbHelperNumero = new DBHelper(mContext);
                                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '1' WHERE CargoId = 1");
                                dbNro.close();
                            } catch (Exception eew){}

                            break;
                        case R.id.radio_cargaSuelta:

                            try {
                                DBHelper dbHelperNumero = new DBHelper(mContext);
                                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '2' WHERE CargoId = 1");
                                dbNro.close();
                            } catch (Exception eew){}

                            break;
                        case R.id.radio_vacio:

                            try {
                                DBHelper dbHelperNumero = new DBHelper(mContext);
                                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '3' WHERE CargoId = 1");
                                dbNro.close();
                            } catch (Exception eew){}

                            break;
                        case R.id.radio_lleno:

                            try {
                                DBHelper dbHelperNumero = new DBHelper(mContext);
                                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '4' WHERE CargoId = 1");
                                dbNro.close();
                            } catch (Exception eew){}

                            break;
                    }
                }
            });


            String vdni = null, vplaca = null;

            try {
                DBHelper dataBaseHelper = new DBHelper(this);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT Placa, Dni FROM Cargo";

                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {
                    vplaca = c.getString(c.getColumnIndex("Placa"));
                    vdni = c.getString(c.getColumnIndex("Dni"));
                }
                c.close();
                dbst.close();

            } catch (Exception e) {}

            if (vplaca==null){
                limpiarDatos();
                return;
            }


            poblarPrimeraVista();
            segundo_btn_fotos_aux();
            cuarto_btn_fotos_aux();


        }

        if (position == 1){

            segundo_edt_or = (EditText) viewPager.findViewById(R.id.segundo_edt_or);
            segundo_edt_cta_bultos = (EditText) viewPager.findViewById(R.id.segundo_edt_cta_bultos);

            segundo_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.segundo_txt_ingreso_tracto);
            segundo_txt_carga = (TextView) viewPager.findViewById(R.id.segundo_txt_carga);
            segundo_txt_dni = (TextView) viewPager.findViewById(R.id.segundo_txt_dni);

            check_carga = (CheckBox) viewPager.findViewById(R.id.sengundo_check_carga);

            if (sTipoCarga.equalsIgnoreCase("1")){
                isSinCarga = true;
                check_carga.setEnabled(false);
                segundo_edt_cta_bultos.setEnabled(false);
                segundo_edt_cta_bultos.setVisibility(View.GONE);

                segundo_edt_cta_bultos.setText("0");
                check_carga.setChecked(false);
                check_carga.setVisibility(View.GONE);

                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Cargo SET CantidadBultos = " + null);
                dba.execSQL("UPDATE Cargo SET isCarga = 'false'");
                dba.close();


            } else {
                isSinCarga = false;
                check_carga.setEnabled(true);
                check_carga.setVisibility(View.VISIBLE);
                segundo_edt_cta_bultos.setVisibility(View.VISIBLE);
                segundo_edt_cta_bultos.setEnabled(true);
                segundo_edt_cta_bultos.setText("");
            }

            poblarSegundaVista();

        }

        if (position == 2){

            tercer_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.tercero_txt_ingreso_tracto);
            tercer_txt_carga = (TextView) viewPager.findViewById(R.id.tercero_txt_carga);
            tercer_txt_dni = (TextView) viewPager.findViewById(R.id.tercero_txt_dni);

            tercero_cargo_text_foto_panoramica = (TextView)viewPager.findViewById(R.id.tercero_cargo_text_foto_panoramica);

            edt_trasera = (LinearLayout)viewPager.findViewById(R.id.edt_trasera);


            if (sTipoCarga.equalsIgnoreCase("1")){

                edt_trasera.setVisibility(View.GONE);
                tercero_cargo_text_foto_panoramica.setText("Trasera");

                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Cargo SET fotoTracera = " + null);
                dba.close();

            } else {
                edt_trasera.setVisibility(View.VISIBLE);
            }

            btn_visualizar_delantera = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_delantera);
            btn_visualizar_trasera = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_trasera);
            btn_visualizar_panoramica = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_panoramica);

            btn_visualizar_delantera.setVisibility(View.GONE);
            btn_visualizar_trasera.setVisibility(View.GONE);
            btn_visualizar_panoramica.setVisibility(View.GONE);

            poblarTerceraVista();

        }

        if (position == 3){

            cuarto_txt_ingreso_tracto  = (TextView) viewPager.findViewById(R.id.cuarto_txt_ingreso_tracto);
            cuarto_txt_carga = (TextView) viewPager.findViewById(R.id.cuarto_txt_carga);
            cuarto_txt_dni = (TextView) viewPager.findViewById(R.id.cuarto_txt_dni);

            cuarto_edt_codContenedor = (EditText) viewPager.findViewById(R.id.cuarto_edt_codContenedor);


            cuarto_edt_precinto = (EditText) viewPager.findViewById(R.id.cuarto_edt_precinto);

            cuarto_edt_origen = (EditText) viewPager.findViewById(R.id.cuarto_edt_origen);
            cuarto_edt_or = (EditText) viewPager.findViewById(R.id.cuarto_edt_or);

            cuarto_switch_tamanoContenedor = (SwitchCompat) viewPager.findViewById(R.id.cuarto_switch_tamanoContenedor);
            cuarto_switch_tipoDoc = (SwitchCompat) viewPager.findViewById(R.id.cuarto_switch_tipoDoc);

            cuarto_switch_tamanoContenedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked){
                        //Toast.makeText(mContext, " c trueuarto_switch_tamanoContenedor", Toast.LENGTH_SHORT).show();

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET tamanoContenedor = '40'");
                            dba.close();
                        } catch (Exception eew){}

                    } else {

                        //Toast.makeText(mContext, " ELSE trueuarto_switch_ztamanoContenedor ", Toast.LENGTH_SHORT).show();
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET tamanoContenedor = '20'");
                            dba.close();
                        } catch (Exception eew){}
                    }
                    //Toast.makeText(ScanCode.this, "Is checked? "+swCarga.isChecked(), Toast.LENGTH_SHORT).show();

                }
            });

            cuarto_switch_tipoDoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked){
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET tipoDocumento = '2'");
                            dba.close();
                        } catch (Exception eew){}

                    } else {

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET tipoDocumento = '1'");
                            dba.close();
                        } catch (Exception eew){}
                    }
                }
            });

            cuarto_switch_tipoDoc.setEnabled(true);
            if (isIngresoV.equalsIgnoreCase("false") && TipoCarga.equalsIgnoreCase("4")){
                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET tipoDocumento = '1'");
                    dba.close();
                } catch (Exception eew){}

                cuarto_switch_tipoDoc.setEnabled(false);
                cuarto_switch_tipoDoc.setChecked(false);
            }

            poblarCuartaVista();

        }

        if (position == 4){

            quinto_txt_ingreso_tracto  = (TextView) viewPager.findViewById(R.id.quinto_txt_ingreso_tracto);
            quinto_txt_carga = (TextView) viewPager.findViewById(R.id.quinto_txt_carga);
            quinto_txt_dni = (TextView) viewPager.findViewById(R.id.quinto_txt_dni);
            quinto_txt_nro_precintos = (TextView) viewPager.findViewById(R.id.quinto_txt_nro_precintos);
            //quinto_btn_precintos = (ImageView) viewPager.findViewById(R.id.quinto_btn_precintos);

            edt_panoramica = (LinearLayout) viewPager.findViewById(R.id.edt_panoramica);

            if (sTipoCarga.equalsIgnoreCase("4")){
                edt_panoramica.setVisibility(View.GONE);
            }

            btn_visualizar_delantera = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_delantera);
            btn_visualizar_trasera = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_trasera);
            btn_visualizar_panoramica = (ImageButton) viewPager.findViewById(R.id.btn_visualizar_panoramica);

            btn_visualizar_delantera.setVisibility(View.GONE);
            btn_visualizar_trasera.setVisibility(View.GONE);
            btn_visualizar_panoramica.setVisibility(View.GONE);

            poblarQuintaVista();

        }

    }

    View.OnClickListener rbSinCargaClick = new View.OnClickListener(){
        public void onClick(View v) {

            try {
                DBHelper dbHelperNumero = new DBHelper(mContext);
                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '1' WHERE CargoId = 1");
                dbNro.close();
            } catch (Exception eew){}

            rbSinCarga.setChecked(true);
            rbCargaSuelta.setChecked(false);
            rbContenedorLleno.setChecked(false);
            rbContenedorVacio.setChecked(false);
        }
    };

    View.OnClickListener rbCargaSueltaClick = new View.OnClickListener(){
        public void onClick(View v) {


            try {
                DBHelper dbHelperNumero = new DBHelper(mContext);
                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '2' WHERE CargoId = 1");
                dbNro.close();
            } catch (Exception eew){}

            rbSinCarga.setChecked(false);
            rbCargaSuelta.setChecked(true);
            rbContenedorLleno.setChecked(false);
            rbContenedorVacio.setChecked(false);
        }
    };

    View.OnClickListener rbContenedorVacioClick = new View.OnClickListener(){
        public void onClick(View v) {

            try {
                DBHelper dbHelperNumero = new DBHelper(mContext);
                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '3' WHERE CargoId = 1");
                dbNro.close();
            } catch (Exception eew){}

            rbSinCarga.setChecked(false);
            rbCargaSuelta.setChecked(false);
            rbContenedorLleno.setChecked(false);
            rbContenedorVacio.setChecked(true);

        }
    };

    View.OnClickListener rbContenedorLlenoClick = new View.OnClickListener(){
        public void onClick(View v) {

            try {
                DBHelper dbHelperNumero = new DBHelper(mContext);
                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                dbNro.execSQL("UPDATE Cargo SET TipoCarga = '4' WHERE CargoId = 1");
                dbNro.close();
            } catch (Exception eew){}

            rbSinCarga.setChecked(false);
            rbCargaSuelta.setChecked(false);
            rbContenedorLleno.setChecked(true);
            rbContenedorVacio.setChecked(false);
        }
    };

    @Override
    public void onPageSelected(int position) {

        String Placa = null, Dni = null, NroOR = null, CantidadBultos = null, sTipoCarga = null, isIngresoV = null,
                codContenedor = null, numeroPrecintos = null, origenDestinoC = null, numeroC = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, Dni, GuiaRemision, CantidadBultos, TipoCarga, codigoContenedor, " +
                    "numeroPrecintos, origenDestino, numeroDocumento, isIngreso FROM Cargo";

            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                NroOR = c.getString(c.getColumnIndex("GuiaRemision"));
                CantidadBultos = c.getString(c.getColumnIndex("CantidadBultos"));
                sTipoCarga = c.getString(c.getColumnIndex("TipoCarga"));

                codContenedor = c.getString(c.getColumnIndex("codigoContenedor"));
                numeroPrecintos = c.getString(c.getColumnIndex("numeroPrecintos"));
                origenDestinoC = c.getString(c.getColumnIndex("origenDestino"));
                numeroC = c.getString(c.getColumnIndex("numeroDocumento"));
                isIngresoV = c.getString(c.getColumnIndex("isIngreso"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        if (position == 1){

            if (Placa == null){
                Toast.makeText(mContext, "Buscar Placa ", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

            if (Dni == null){
                Toast.makeText(mContext, "Buscar DNI", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

            if (sTipoCarga.equalsIgnoreCase("3") || sTipoCarga.equalsIgnoreCase("4")){
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 2);
                return;
            }
        }

        if (position == 2){

            if (sTipoCarga.equalsIgnoreCase("3") || sTipoCarga.equalsIgnoreCase("4")){
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 2);
                return;
            }

            if (sTipoCarga.equalsIgnoreCase("1") && NroOR == null){
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                Toast.makeText(mContext, "Ingrese OR/GR", Toast.LENGTH_SHORT).show();
            }

            if (sTipoCarga.equalsIgnoreCase("2") && NroOR == null){
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                Toast.makeText(mContext, "Ingrese OR/GR", Toast.LENGTH_SHORT).show();
            }

        }

        if (position == 3){

            if (sTipoCarga.equalsIgnoreCase("1") || sTipoCarga.equalsIgnoreCase("2")){
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }
        }

        if (position == 4){

            if (codContenedor == null){
                Toast.makeText(mContext, "Falta Código de Contenedor Placa ", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

            if (numeroPrecintos == null){
                Toast.makeText(mContext, "Falta Número Precintos", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

            if (origenDestinoC == null){
                Toast.makeText(mContext, "Falta Origen Destino", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

            if (numeroC == null){
                Toast.makeText(mContext, "Falta Número Documento", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                return;
            }

        }


        if (!sTipoCarga.equalsIgnoreCase("1")){

            if (position == 2 && CantidadBultos == null){
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                Toast.makeText(mContext, "Ingrese CantidadBultos I", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (position == 0){


            primero_edt_tracto = (EditText) viewPager.findViewById(R.id.primero_edt_tracto);
            primero_edt_tracto.requestFocus(); //Asegurar que editText tiene focus
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(primero_edt_tracto, InputMethodManager.SHOW_IMPLICIT);

            primero_edt_dni = (EditText) viewPager.findViewById(R.id.primero_edt_dni);
            primero_txt_mje = (TextView)viewPager.findViewById(R.id.primero_txt_mje);

            check_casco = (CheckBox) viewPager.findViewById(R.id.check_casco);
            check_chaleco = (CheckBox) viewPager.findViewById(R.id.check_chaleco);
            check_botas = (CheckBox) viewPager.findViewById(R.id.check_botas);

            isLicencia = (SwitchCompat)findViewById(R.id.switch_licencia);

            radiogroup =  (RadioGroup) viewPager.findViewById(R.id.opciones_carga);

            rbSinCarga = (RadioButton)viewPager.findViewById(R.id.radio_sinCarga);
            rbCargaSuelta = (RadioButton)viewPager.findViewById(R.id.radio_cargaSuelta);
            rbContenedorLleno = (RadioButton)viewPager.findViewById(R.id.radio_lleno);
            rbContenedorVacio = (RadioButton)viewPager.findViewById(R.id.radio_vacio);

            img_cargo_persona = (ImageView)viewPager.findViewById(R.id.img_cargo_persona);
            ly_foto = (LinearLayout) viewPager.findViewById(R.id.ly_foto);
            //ly_foto.setVisibility(View.GONE);

            cargo_txt_dni_persona = (TextView)viewPager.findViewById(R.id.cargo_txt_dni_persona);
            cargo_txt_empresa_persona = (TextView)viewPager.findViewById(R.id.cargo_txt_empresa_persona);

            cuarto_btn_fotos_aux();

        }

        if (position == 1){

            segundo_edt_or = (EditText) viewPager.findViewById(R.id.segundo_edt_or);
            segundo_edt_or.requestFocus(); //Asegurar que editText tiene focus
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(segundo_edt_or, InputMethodManager.SHOW_IMPLICIT);

            segundo_edt_cta_bultos = (EditText) viewPager.findViewById(R.id.segundo_edt_cta_bultos);
            segundo_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.segundo_txt_ingreso_tracto);
            segundo_txt_carga = (TextView) viewPager.findViewById(R.id.segundo_txt_carga);
            segundo_txt_dni = (TextView) viewPager.findViewById(R.id.segundo_txt_dni);

            check_carga = (CheckBox) viewPager.findViewById(R.id.sengundo_check_carga);

            if (sTipoCarga.equalsIgnoreCase("1")){
                isSinCarga = true;
                check_carga.setEnabled(false);
                segundo_edt_cta_bultos.setEnabled(false);

                segundo_edt_cta_bultos.setText("0");
                check_carga.setChecked(false);

                check_carga.setVisibility(View.GONE);
                segundo_edt_cta_bultos.setVisibility(View.GONE);

                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Cargo SET CantidadBultos = " + null);
                dba.execSQL("UPDATE Cargo SET isCarga = 'false'");
                dba.close();

            } else {
                isSinCarga = false;
                check_carga.setEnabled(true);
                check_carga.setVisibility(View.VISIBLE);
                segundo_edt_cta_bultos.setVisibility(View.VISIBLE);

                segundo_edt_cta_bultos.setEnabled(true);
                segundo_edt_cta_bultos.setText("");
            }

        }

        if (position == 2){

            tercer_txt_ingreso_tracto = (TextView) viewPager.findViewById(R.id.tercero_txt_ingreso_tracto);
            tercer_txt_carga = (TextView) viewPager.findViewById(R.id.tercero_txt_carga);
            tercer_txt_dni = (TextView) viewPager.findViewById(R.id.tercero_txt_dni);
            tercero_cargo_text_foto_panoramica = (TextView)viewPager.findViewById(R.id.tercero_cargo_text_foto_panoramica);

            edt_trasera = (LinearLayout) viewPager.findViewById(R.id.edt_trasera);

            if (sTipoCarga.equalsIgnoreCase("1")){

                edt_trasera.setVisibility(View.GONE);
                tercero_cargo_text_foto_panoramica.setText("Trasera");

                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Cargo SET fotoTracera = " + null);
                dba.close();

            } else {
                edt_trasera.setVisibility(View.VISIBLE);
            }
        }

        if (position == 3){

            cuarto_txt_ingreso_tracto  = (TextView) viewPager.findViewById(R.id.cuarto_txt_ingreso_tracto);
            cuarto_txt_carga = (TextView) viewPager.findViewById(R.id.cuarto_txt_carga);
            cuarto_txt_dni = (TextView) viewPager.findViewById(R.id.cuarto_txt_dni);

            cuarto_edt_codContenedor = (EditText) viewPager.findViewById(R.id.cuarto_edt_codContenedor);

            cuarto_edt_codContenedor.requestFocus(); //Asegurar que editText tiene focus
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cuarto_edt_codContenedor, InputMethodManager.SHOW_IMPLICIT);

            cuarto_edt_precinto = (EditText) viewPager.findViewById(R.id.cuarto_edt_precinto);
            cuarto_edt_origen = (EditText) viewPager.findViewById(R.id.cuarto_edt_origen);
            cuarto_edt_or = (EditText) viewPager.findViewById(R.id.cuarto_edt_or);

            cuarto_switch_tamanoContenedor = (SwitchCompat) viewPager.findViewById(R.id.cuarto_switch_tamanoContenedor);
            cuarto_switch_tipoDoc = (SwitchCompat) viewPager.findViewById(R.id.cuarto_switch_tipoDoc);

            cuarto_switch_tipoDoc.setEnabled(true);
            if (isIngresoV.equalsIgnoreCase("false") && sTipoCarga.equalsIgnoreCase("4")){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET tipoDocumento = '1'");
                    dba.close();
                } catch (Exception eew){}

                cuarto_switch_tipoDoc.setEnabled(false);
                cuarto_switch_tipoDoc.setChecked(false);
            }


        }

        if (position == 4){

            quinto_txt_ingreso_tracto  = (TextView) viewPager.findViewById(R.id.quinto_txt_ingreso_tracto);
            quinto_txt_carga = (TextView) viewPager.findViewById(R.id.quinto_txt_carga);
            quinto_txt_dni = (TextView) viewPager.findViewById(R.id.quinto_txt_dni);
            quinto_txt_nro_precintos = (TextView) viewPager.findViewById(R.id.quinto_txt_nro_precintos);

            //quinto_btn_precintos = (ImageView) viewPager.findViewById(R.id.quinto_btn_precintos);


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
                }else {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET isCarga = 'false'");
                    db.close();
                }
                break;
        }
    }

    public void segundo_btn_fotos(View view){
        segundo_btn_fotos();
    }

    public void segundo_btn_fotos(){

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(segundo_edt_or.getWindowToken(), 0);

        if (segundo_edt_or.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese OR/GR", Toast.LENGTH_SHORT).show();
            return;
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET GuiaRemision = '"+segundo_edt_or.getText().toString()+"'");
            db.close();
        }

        String sTipoCarga = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT TipoCarga FROM Cargo";

            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                sTipoCarga = c.getString(c.getColumnIndex("TipoCarga"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (!sTipoCarga.equalsIgnoreCase("1")){

            if (segundo_edt_cta_bultos.getText().toString().matches("")){
                Toast.makeText(mContext, "Ingrese cantidad de bultos", Toast.LENGTH_SHORT).show();
                return;

            } else {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                db.execSQL("UPDATE Cargo SET CantidadBultos = "+segundo_edt_cta_bultos.getText().toString()+"");
                db.close();
            }

        }

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

    }

    public void segundo_btn_fotos_aux(){

        if (segundo_edt_or == null || segundo_edt_or.getText().toString().matches("")){
            Log.e(" segundo_btn ", "_fotos_aux");
        }

        String sTipoCarga = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT TipoCarga FROM Cargo";

            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                sTipoCarga = c.getString(c.getColumnIndex("TipoCarga"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (!sTipoCarga.equalsIgnoreCase("1")){

            if (segundo_edt_cta_bultos == null ||  segundo_edt_cta_bultos.getText().toString().matches("")){
            } else {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                db.execSQL("UPDATE Cargo SET CantidadBultos = "+segundo_edt_cta_bultos.getText().toString()+"");
                db.close();
            }
        }
    }

    public void cuarto_btn_fotos(View view){
        cuarto_btn_fotos();
    }

    public void cuarto_btn_fotos(){

        if (cuarto_edt_codContenedor.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese código de contenedor", Toast.LENGTH_SHORT).show();
            return;
        } else if (cuarto_edt_codContenedor.getText().toString().length() <= 10){
            Toast.makeText(mContext, "Contenedor debe tener 11 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET codigoContenedor = '"+cuarto_edt_codContenedor.getText().toString()+"'");
            db.close();
        }


        if (cuarto_edt_precinto.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese cantidad de precintos", Toast.LENGTH_SHORT).show();
            return;
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET numeroPrecintos = "+cuarto_edt_precinto.getText().toString()+"");
            db.close();
        }

        if (cuarto_edt_origen.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese Origen/Destino", Toast.LENGTH_SHORT).show();
            return;
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET origenDestino = '"+cuarto_edt_origen.getText().toString()+"'");
            db.close();
        }

        if (cuarto_edt_or.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese Número", Toast.LENGTH_SHORT).show();
            return;
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET numeroDocumento = "+cuarto_edt_or.getText().toString()+"");
            db.close();
        }


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(cuarto_edt_codContenedor.getWindowToken(), 0);

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

    }

    public void cuarto_btn_fotos_aux(){

        if (cuarto_edt_codContenedor == null
                || cuarto_edt_codContenedor.getText().toString().matches("")
                || cuarto_edt_codContenedor.getText().length() <=10){

            //Toast.makeText(mContext, "El código del contenedor debe tener 11 caracteres.", Toast.LENGTH_SHORT).show();


        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET codigoContenedor = '"+cuarto_edt_codContenedor.getText().toString()+"'");
            db.close();
        }


        if (cuarto_edt_precinto == null
                || cuarto_edt_precinto.getText().toString().matches("")){
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET numeroPrecintos = "+cuarto_edt_precinto.getText().toString()+"");
            db.close();
        }

        if (cuarto_edt_origen == null
                || cuarto_edt_origen.getText().toString().matches("")){
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET origenDestino = '"+cuarto_edt_origen.getText().toString()+"'");
            db.close();
        }

        if (cuarto_edt_or == null
                ||  cuarto_edt_or.getText().toString().matches("")){
        } else {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Cargo SET numeroDocumento = "+cuarto_edt_or.getText().toString()+"");
            db.close();
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
                }else {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppCasco = 'false'");
                    db.close();
                }

                break;
            case R.id.check_chaleco:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppChaleco = 'true'");
                    db.close();
                }else{
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppChaleco = 'false'");
                    db.close();
                }

            case R.id.check_botas:
                if (checked){
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppBotas = 'true'");
                    db.close();
                }else{
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Cargo SET EppBotas = 'false'");
                    db.close();
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


    public void enviarPlaca(View view){
        enviarPlaca();
    }

    public void enviarPlaca() {

        String pla = null;

        if (primero_edt_tracto.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese PLaca", Toast.LENGTH_SHORT).show();
            return;
        }

        if (primero_edt_tracto.getText().length() < 6 ){
            Toast.makeText(mContext, "La placa debe tener un mínimo de 6 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            DBHelper dbHelperNumero = new DBHelper(this);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            dbNro.execSQL("UPDATE Cargo SET Placa = '"+primero_edt_tracto.getText().toString()+"' WHERE CargoId = 1");
            dbNro.close();

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

        pla = primero_edt_tracto.getText().toString();

        if (true){

            String URL = URL_API.concat("api/Cargo/VerificaPlaca?Placa="+primero_edt_tracto.getText().toString()+"&DispositivoId="+GuidDipositivo+"");

            ProgressDialog pDialog;

            pDialog = new ProgressDialog(CargoActivity.this);
            pDialog.setMessage("Verificando Placa...");
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

                                if (!result.get("CargoTipoMovimientoId").isJsonNull()){

                                    if (result.get("CargoTipoMovimientoId").getAsString().equalsIgnoreCase("1")){
                                        try {
                                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                            dba.execSQL("UPDATE Cargo SET isIngreso = 'true'");
                                            dba.close();

                                            Log.e("UPDATE Cargo SET ", "isIngreso = 'true'");

                                        } catch (Exception eew){
                                            Log.e("Exceptoion true ", "");
                                        }

                                        //
                                        primero_txt_mje.setText("El Tracto "+primero_edt_tracto.getText().toString()+" está Ingresando");
                                    } else if (result.get("CargoTipoMovimientoId").getAsString().equalsIgnoreCase("2")) {
                                        try {
                                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                            dba.execSQL("UPDATE Cargo SET isIngreso = 'false'");
                                            dba.close();

                                            Log.e("UPDATE Cargo SET ", "isIngreso = 'false'");

                                        } catch (Exception eew){
                                            Log.e("Exceptoion false ", "");
                                        }

                                        primero_txt_mje.setText("El Tracto "+primero_edt_tracto.getText().toString()+" está Saliendo");
                                    }

                                }

                                if (result.get("Placa").isJsonNull()){
                                    primero_txt_mje.setText("");

                                    //if (result.get("Registrado").getAsString().equalsIgnoreCase("0")){

                                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);

                                    View mView = getLayoutInflater().inflate(R.layout.dialog_placa_failed, null);
                                    TextView texMje = (TextView)mView.findViewById(R.id.mje_placa_ok);
                                    texMje.setText("La placa "+primero_edt_tracto.getText().toString()+" no ha sido registrada. " +
                                            "¿Desea registar la placa?");

                                    try {

                                        mBuilder.setCancelable(false);
                                        mBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                                ejecutarApiRegistro(primero_edt_tracto.getText().toString(), GuidDipositivo);

                                            }
                                        });

                                        mBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                limpiarDatos();
                                                dialog.dismiss();
                                            }
                                        });

                                        mBuilder.setView(mView);
                                        AlertDialog dialog = mBuilder.create();
                                        dialog.show();
                                    } catch (Exception vfdbe) {
                                        e.printStackTrace();
                                    }
                                    //}
                                }

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }

                                } catch (Exception edd){

                                }

                            } else  {
                                //Toast.makeText(CargoActivity.this, "Error. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();
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
            return;
        }

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                if (btn_dni){
                    isScanner = true;
                    valor = result.getContents();
                    formato = result.getFormatName();
                    enviarDNI();
                }
            }
        }

        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {
            Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));

            String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            Uri_Foto = photoUri;

            if (isPrecinto){

                try {

                    DBHelper dbHelperNumero = new DBHelper(this);
                    SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                    dbNro.execSQL("UPDATE CargoPrecinto SET Foto = '"+Uri_Foto+"' WHERE Indice = '"+numeroPrecintoFoto+"'");
                    dbNro.close();
                } catch (Exception eew){}


                loadPrecinto();

                return;
            }

            if (fotoDelantera){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoDelantera = '"+Uri_Foto+"'");
                    dba.close();
                } catch (Exception eew){
                    Log.e("Exception ", "fotoDelantera");
                }

                //imgEstadoDelantera.setImageResource(R.drawable.ic_check_foto);
                btn_visualizar_delantera.setVisibility(View.VISIBLE);
                btn_visualizar_delantera.setImageURI(Uri.parse(getRightAngleImage(Uri_Foto)));

                fotoDelantera = false;


            } else if (fotoTracera){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoTracera = '"+Uri_Foto+"'");
                    dba.close();
                } catch (Exception eew){
                    Log.e("Exception ", "fotoTracera");
                }

                //imgEstadoTrasera.setImageResource(R.drawable.ic_check_foto);
                btn_visualizar_trasera.setVisibility(View.VISIBLE);
                btn_visualizar_trasera.setImageURI(Uri.parse(getRightAngleImage(Uri_Foto)));
                fotoTracera = false;

            } else {

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Cargo SET fotoPanoramica = '"+Uri_Foto+"'");
                    dba.close();
                } catch (Exception eew){
                    Log.e("Exception ", "fotoPanoramica");
                }

                //imgEstadoPaniramica.setImageResource(R.drawable.ic_check_foto);
                btn_visualizar_panoramica.setVisibility(View.VISIBLE);
                btn_visualizar_panoramica.setImageURI(Uri.parse(getRightAngleImage(Uri_Foto)));
                fotoPanoramica = false;
            }
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

        if (true){

            if (!isScanner){
                valor = primero_edt_dni.getText().toString();
            }

            Log.e("-------- ", "--------");
            Log.e("GuidDipositivo ", GuidDipositivo);
            Log.e("NroDOI ", valor);


            String URL = URL_API.concat("api/People/VerificaDOI?NroDOI="+valor+
                    "&DispositivoId="+GuidDipositivo+"");

            final ProgressDialog pDialog;

            pDialog = new ProgressDialog(CargoActivity.this);
            pDialog.setMessage("Obteniendo Datos...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            Ion.with(this)
                    .load("GET", URL)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> response) {

                            if(e != null){

                                Log.e("Exception ", "VerificaDOI ");
                                limpiarDatosRadioBoton();
                                limpiarDatos();

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                                    Toast.makeText(mContext, "¡Ha ocurrido un problema!. Comuníquese con su administrador.", Toast.LENGTH_LONG).show();

                                } catch (Exception esc){}
                                return;

                            }

                            if(response == null){

                                Toast.makeText(mContext, "¡No hubo respuesta del servidor!. Por favor intente nuevamente. Caso contrario comuníquese con su administrador.", Toast.LENGTH_LONG).show();

                                try {

                                    if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                                } catch (Exception esc){}
                                return;

                            }

                            if (response.getHeaders().code() == 200) {

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);
                                Log.e("JsonObject PEOPLE ", result.toString());

                                if (!result.get("Resultado").isJsonNull()){

                                    Log.e(" RESULTADO ", result.get("Resultado").getAsString());

                                    if (result.get("Resultado").getAsString().equalsIgnoreCase("ERROR")){
                                        if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                                        MensajeError(result.get("Header").getAsString(), result.get("Mensaje").getAsString());
                                        Log.e("ERROR ","ERROR");
                                        return;
                                    }


                                    if (result.get("Resultado").getAsString().equalsIgnoreCase("OK")){

                                        if (!result.get("Img").isJsonNull()){

                                            //ly_foto.setVisibility(View.VISIBLE);

                                            try {
                                                Ion.with(img_cargo_persona)
                                                        .placeholder(R.drawable.ic_foto_fail)
                                                        .error(R.drawable.ic_foto_fail)
                                                        .load(result.get("Img").getAsString());

                                            } catch (Exception esvd){}
                                        } else {
                                            //ly_foto.setVisibility(View.GONE);
                                        }


                                        if (!result.get("persNombres").isJsonNull()){
                                            primero_edt_dni.setText(result.get("NroDOI").getAsString());
                                            cargo_txt_dni_persona.setText(result.get("persNombres").getAsString());
                                            cargo_txt_empresa_persona.setText(result.get("persEmpresa").getAsString());
                                        }

                                        try {
                                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                            dba.execSQL("UPDATE Cargo SET Dni = "+result.get("NroDOI").getAsString()+"");
                                            dba.execSQL("UPDATE Cargo SET json = '"+String.valueOf(result.toString())+"'");
                                            dba.close();

                                            if (!result.get("Mensaje").isJsonNull()){
                                                MensajePersona(result.get("Mensaje").getAsString());
                                            }

                                        } catch (Exception eew){
                                            Log.e("Exception ", "CargoActivity Dni");
                                        }
                                    } else {
                                        primero_edt_dni.setFocusable(true);
                                    }

                                } else {

                                    primero_edt_dni.setFocusable(true);
                                    limpiarDatosRadioBoton();
                                    limpiarDatos();
                                    mensajePersona();

                                }

                            } else {
                                Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
                            }

                            try {

                                if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}

                            } catch (Exception esc){}

                        }
                    });
        }

    }

    public void buscarDNI(View view){

        if (primero_edt_dni.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese DNI", Toast.LENGTH_SHORT).show();
            return;
        }

        if (primero_edt_dni.getText().length() < 8 ){
            Toast.makeText(mContext, "El DNI debe tener un mínimo de 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        isScanner = false;
        valor = primero_edt_dni.getText().toString();
        enviarDNI();

    }

    public void poblarPrimeraVista(){

        String Placa = null, TipoCarga = null, Dni = null, isCarga = null, isIngresoS = null,
                EppCasco = null, EppChaleco = null, EppBotas = null, isLicenciaL = null, json = null;
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

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT json FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                json = c.getString(c.getColumnIndex("json"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        if (TipoCarga.equalsIgnoreCase("1")){
            rbSinCarga.setChecked(true);
            rbCargaSuelta.setChecked(false);
            rbContenedorVacio.setChecked(false);
            rbContenedorLleno.setChecked(false);

        } else if (TipoCarga.equalsIgnoreCase("2")){
            rbCargaSuelta.setChecked(true);
            rbSinCarga.setChecked(false);
            rbContenedorVacio.setChecked(false);
            rbContenedorLleno.setChecked(false);

        } else if (TipoCarga.equalsIgnoreCase("3")){
            rbContenedorVacio.setChecked(true);
            rbCargaSuelta.setChecked(false);
            rbSinCarga.setChecked(false);
            rbContenedorLleno.setChecked(false);

        } else if (TipoCarga.equalsIgnoreCase("4")){
            rbContenedorLleno.setChecked(true);
            rbContenedorVacio.setChecked(false);
            rbCargaSuelta.setChecked(false);
            rbSinCarga.setChecked(false);

        }

        if (EppCasco.equalsIgnoreCase("true")){ check_casco.setChecked(true);}

        if (EppChaleco.equalsIgnoreCase("true")){ check_chaleco.setChecked(true);}

        if (EppBotas.equalsIgnoreCase("true")){ check_botas.setChecked(true);}

        if (isLicenciaL.equalsIgnoreCase("false")){ isLicencia.setChecked(true);}

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

        if (json==null){
            Log.e(" json == ", "NULL");
            return;
        }


        //ly_foto.setVisibility(View.VISIBLE);
        Gson gson = new Gson();
        JsonObject result = gson.fromJson(json, JsonObject.class);
        cargo_txt_dni_persona.setText(result.get("persNombres").getAsString());
        cargo_txt_empresa_persona.setText(result.get("persEmpresa").getAsString());

        try {
            Ion.with(img_cargo_persona)
                    .placeholder(R.drawable.ic_foto_fail)
                    .error(R.drawable.ic_foto_fail)
                    .load(result.get("Img").getAsString());

        } catch (Exception esvd){}

    }

    public void poblarSegundaVista(){

        String Placa = null, TipoCarga = null, Dni = null, isCarga = null, isIngreso = null,
        or = null, ctaBultos = null;

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, isCarga, GuiaRemision, CantidadBultos FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));
                isCarga = c.getString(c.getColumnIndex("isCarga"));
                or = c.getString(c.getColumnIndex("GuiaRemision"));
                ctaBultos = c.getString(c.getColumnIndex("CantidadBultos"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){
            return;
        }

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
            segundo_edt_or.setText("");
            return;
        }
        segundo_edt_or.setText(or);

        if (ctaBultos == null){
            segundo_edt_cta_bultos.setText("");
            return;
        }

        segundo_edt_cta_bultos.setText(ctaBultos);

    }

    public void poblarTerceraVista(){

        String Placa = null, TipoCarga = null, Dni = null, numeroPrecintos = null, isIngreso = null,
                fotoD=null, fotoT=null, fotoP=null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, " +
                    "fotoDelantera, fotoTracera, fotoPanoramica FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));

                fotoD = c.getString(c.getColumnIndex("fotoDelantera"));
                fotoT = c.getString(c.getColumnIndex("fotoTracera"));
                fotoP = c.getString(c.getColumnIndex("fotoPanoramica"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){
            return;
        }

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

    public void poblarCuartaVista(){

        String Placa = null, TipoCarga = null, stipoDocumento = null, Dni = null, stamanoContenedor = null,
                isIngreso = null, codigoContenedor = null, numeroPrecintos = null, origenDestino = null,
                numeroDocumento = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, tamanoContenedor, tipoDocumento," +
                    "codigoContenedor, numeroPrecintos, origenDestino, numeroDocumento FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));
                stamanoContenedor = c.getString(c.getColumnIndex("tamanoContenedor"));
                stipoDocumento = c.getString(c.getColumnIndex("tipoDocumento"));

                codigoContenedor = c.getString(c.getColumnIndex("codigoContenedor"));
                numeroPrecintos = c.getString(c.getColumnIndex("numeroPrecintos"));
                origenDestino = c.getString(c.getColumnIndex("origenDestino"));
                numeroDocumento = c.getString(c.getColumnIndex("numeroDocumento"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){return;}

        if (isIngreso.equalsIgnoreCase("true")){cuarto_txt_ingreso_tracto.setText("Ingreso de Tracto "+Placa);
        } else {cuarto_txt_ingreso_tracto.setText("Salida de Tracto "+Placa);}

        if (TipoCarga.equalsIgnoreCase("1")){cuarto_txt_carga.setText("Sin Carga");
        } else if (TipoCarga.equalsIgnoreCase("2")){cuarto_txt_carga.setText("Carga Suelta");
        } else if (TipoCarga.equalsIgnoreCase("3")){cuarto_txt_carga.setText("Contenedor Vacío");
        }else {cuarto_txt_carga.setText("Contenedor LLeno");}

        cuarto_txt_dni.setText("Conductor con DNI "+ Dni + "");

        if (stamanoContenedor.equalsIgnoreCase("40")){ cuarto_switch_tamanoContenedor.setChecked(true);}

        if (stipoDocumento.equalsIgnoreCase("2")){ cuarto_switch_tipoDoc.setChecked(true);}


        if (codigoContenedor == null){return;}
        cuarto_edt_codContenedor.setText(codigoContenedor);

        if (numeroPrecintos == null){return;}
        cuarto_edt_precinto.setText(numeroPrecintos);

        if (origenDestino == null){return;}
        cuarto_edt_origen.setText((origenDestino));

        if (numeroDocumento == null){return;}
        cuarto_edt_or.setText(numeroDocumento);

    }

    public void poblarQuintaVista(){

        int contadorIndice = 0, candFotos = 0;

        String numeroPrecintos = null, isIngreso = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, numeroPrecintos " +
                    "FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                isIngreso = c.getString(c.getColumnIndex("isIngreso"));
                numeroPrecintos = c.getString(c.getColumnIndex("numeroPrecintos"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (Placa == null){
            return;
        }

        if (isIngreso.equalsIgnoreCase("true")){
            quinto_txt_ingreso_tracto.setText("Ingreso de Tracto "+Placa);
        } else {
            quinto_txt_ingreso_tracto.setText("Salida de Tracto "+Placa);
        }

        if (TipoCarga.equalsIgnoreCase("1")){quinto_txt_carga.setText("Sin Carga");
        } else if (TipoCarga.equalsIgnoreCase("2")){quinto_txt_carga.setText("Carga Suelta");
        } else if (TipoCarga.equalsIgnoreCase("3")){quinto_txt_carga.setText("Contenedor Vacío");
        }else {quinto_txt_carga.setText("Contenedor LLeno");}

        quinto_txt_dni.setText("Conductor con DNI "+ Dni + "");


        try {
            DBHelper bdh = new DBHelper(this);
            SQLiteDatabase sqlite = bdh.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto WHERE Foto IS NOT NULL";
            Cursor ca = sqlite.rawQuery(selectQuery, new String[]{});
            candFotos = ca.getCount();
            ca.close();
            sqlite.close();

        } catch (Exception e) {}

        quinto_txt_nro_precintos.setText("Precintos: 0 de "+ candFotos);

        /*if (TipoCarga.equalsIgnoreCase("3")){
            quinto_btn_precintos.setEnabled(false);
            quinto_btn_precintos.setVisibility(View.GONE);

        } else {
            quinto_btn_precintos.setEnabled(true);
            quinto_btn_precintos.setVisibility(View.VISIBLE);
            loadPrecinto();
        }*/

        // CREACION DE PRE FOTOS -------------------------------------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Indice FROM CargoPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorIndice = c.getCount();
            /*if (c.moveToFirst()) {

                do {
                    dataModelsMovil.add(new PrecintoDataModel(c.getString(c.getColumnIndex("Foto"))));
                } while (c.moveToNext());

            }*/
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contadorIndice==0){

            for (int i = 1; i <= Integer.parseInt(numeroPrecintos) ; i++){

                try {

                    CargoPrecintoCrud cargoPrecintoCrud = new CargoPrecintoCrud(mContext);
                    CargoPrecinto cargoPrecinto = new CargoPrecinto();
                    cargoPrecinto.Indice = "Precinto No "+ String.valueOf(i);
                    cargoPrecinto.CargoPrecintoId = _CargoPrecinto_Id;
                    _CargoPrecinto_Id = cargoPrecintoCrud.insert(cargoPrecinto);
                } catch (Exception esca) {esca.printStackTrace();}

            }
        }


        //quinto_btn_precintos.setEnabled(true);
        //quinto_btn_precintos.setVisibility(View.VISIBLE);
        loadPrecinto();

    }

    public void parteDelantera(View view){
        fotoDelantera = true;
        fotoTracera = false;
        fotoPanoramica = false;

        isPrecinto = false;

        tomarFoto();
    }

    public void parteTracera(View view){
        fotoDelantera = false;
        fotoTracera = true;
        fotoPanoramica = false;

        isPrecinto = false;

        tomarFoto();
    }

    public void partePanoramica(View view){
        fotoDelantera = false;
        fotoTracera = false;
        fotoPanoramica = true;

        isPrecinto = false;

        tomarFoto();
    }

    // METODOS DE CAMARA
    public void tomarFoto(){

        photoUri = null;

        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_LOWEST)
                .enableImageCropping(false)
                .launchCamera();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(CargoActivity.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                viewPager.setCurrentItem(viewPager.getCurrentItem() - 2);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void returnPersona(View view){
        segundo_btn_fotos_aux();
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public void returnPersonaCuarto(View view){
        cuarto_btn_fotos_aux();
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 3);
    }

    public void quintoReturnPersona(View view){
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 4);
    }

    public void returnCarga(View view){
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    public void primeroViewCarga(View view){

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

        String TipoCarga = null;
        int selectedId = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT TipoCarga FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                selectedId = c.getInt(c.getColumnIndex("TipoCarga"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (selectedId==1){
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

        } else if(selectedId==2){
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

        } else if(selectedId==3){
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 3);

        } else if (selectedId==4){
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 3);

        }

    }

    public void primeroViewFoto(View view){

    }

    public void terceroReturnPersona(View view){viewPager.setCurrentItem(viewPager.getCurrentItem() - 2);}

    public void finalizarSend(View view){

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
                    "Dni, isLicencia, GuiaRemision, isCarga, CantidadBultos, isIngreso, fotoDelantera, " +
                    "fotoTracera, fotoPanoramica FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Casco = c.getString(c.getColumnIndex("EppCasco"));
                Chaleco = c.getString(c.getColumnIndex("EppChaleco"));
                Botas = c.getString(c.getColumnIndex("EppBotas"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                Licencia = c.getString(c.getColumnIndex("isLicencia"));
                NroOR = c.getString(c.getColumnIndex("GuiaRemision"));
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

        if (TipoCarga.equalsIgnoreCase("1")){
            SinCarga();

        } else if (TipoCarga.equalsIgnoreCase("2")){
            CargaSuelta();

        } else if (TipoCarga.equalsIgnoreCase("3")){
            ContenedorVacio();

        } else {
            ContenedorLLeno();
        }
    }

    public void finalizarContenedorSend(View view){

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
                    "Dni, isLicencia, numeroDocumento, tamanoContenedor, codigoContenedor, " +
                    "numeroPrecintos, origenDestino, tipoDocumento, fotoPanoramica, fotoDelantera, " +
                    "fotoTracera, isIngreso FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                Placa = c.getString(c.getColumnIndex("Placa"));
                TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
                Casco = c.getString(c.getColumnIndex("EppCasco"));
                Chaleco = c.getString(c.getColumnIndex("EppChaleco"));
                Botas = c.getString(c.getColumnIndex("EppBotas"));
                Dni = c.getString(c.getColumnIndex("Dni"));
                Licencia = c.getString(c.getColumnIndex("isLicencia"));
                sNumero = c.getString(c.getColumnIndex("numeroDocumento"));

                sCodigoContenedor = c.getString(c.getColumnIndex("codigoContenedor"));
                sNumeroPrecinto = c.getString(c.getColumnIndex("numeroPrecintos"));
                sOrigen = c.getString(c.getColumnIndex("origenDestino"));
                stamañoContenedor = c.getString(c.getColumnIndex("tamanoContenedor"));
                sTipoDocumento = c.getString(c.getColumnIndex("tipoDocumento"));
                Delantera = c.getString(c.getColumnIndex("fotoDelantera"));
                Trasera = c.getString(c.getColumnIndex("fotoTracera"));
                Panoramica = c.getString(c.getColumnIndex("fotoPanoramica"));
                CargoTipoMovimiento = c.getString(c.getColumnIndex("isIngreso"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (TipoCarga.equalsIgnoreCase("1")){
            SinCarga();

        } else if (TipoCarga.equalsIgnoreCase("2")){
            CargaSuelta();

        } else if (TipoCarga.equalsIgnoreCase("3")){
            ContenedorVacio();

        } else {
            ContenedorLLeno();
        }
    }

    public void fotoPrecinto(String numeroPrecinto){

        /*int ctaA = 0,ctaB = 0;
        try {
            DBHelper bdh = new DBHelper(this);
            SQLiteDatabase sqlite = bdh.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto WHERE Foto IS NOT NULL";
            Cursor ca = sqlite.rawQuery(selectQuery, new String[]{});
            ctaA = ca.getCount();
            ca.close();
            sqlite.close();

        } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT numeroPrecintos FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                ctaB = c.getInt(c.getColumnIndex("numeroPrecintos"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (ctaA == ctaB){
            Toast.makeText(mContext, "¡Precintos Completos!", Toast.LENGTH_SHORT).show();
            return;
        }*/

        numeroPrecintoFoto = numeroPrecinto;
        isPrecinto = true;
        tomarFoto();


    }

    public void SinCarga(){

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

        if (Delantera==null){
            Toast.makeText(mContext, "Falta Foto Delantera", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Panoramica==null){
            Toast.makeText(mContext, "Falta Foto Panoramica - Trasera (Lado derecho)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TipoCarga.equalsIgnoreCase("1")){
            CantidadBultos = "0";
        }


        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/CreateSinCarga");

        Log.e("Numero", Numero);
        Log.e("DispositivoId", DispositivoId);
        Log.e("Placa", Placa);
        Log.e("CargoTipoMovimientoId", CargoTipoMovimiento);
        Log.e("CargoTipoCargaId", TipoCarga);
        Log.e("Casco", Casco);
        Log.e("Chaleco", Chaleco);
        Log.e("Botas", Botas);
        Log.e("Licencia ", Licencia);
        Log.e("NroDOI", Dni);
        Log.e("NroORGR", NroOR);
        Log.e("Delantera", Delantera);
        Log.e("Panoramica", Panoramica);

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
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
                .setMultipartFile("Delantera", new File(Delantera))
                .setMultipartFile("Panoramica", new File(Panoramica))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if (e != null){

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
                            }
                            return;
                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            if (result.get("Estado").getAsBoolean()){

                                limpiarDatos();
                                try {
                                    showDialogSend();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {

                                Toast.makeText(mContext, result.get("Exception").getAsString(), Toast.LENGTH_SHORT).show();
                            }

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

                    }
                });

    }

    public void CargaSuelta(){

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

        if (TipoCarga.equalsIgnoreCase("1")){
            CantidadBultos = "0";
        }


        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/CreateCargaSuelta");

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
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
                .setMultipartFile("Delantera", new File(Delantera))
                .setMultipartFile("Trasera", new File(Trasera))
                .setMultipartFile("Panoramica", new File(Panoramica))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if (e != null){

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                            Log.e("Excepction ConCarga", e.toString());

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
                            }

                            return;
                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            if (result.get("Estado").getAsBoolean()){

                                limpiarDatos();

                                try {
                                    showDialogSend();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {

                                Toast.makeText(mContext, result.get("Exception").getAsString(), Toast.LENGTH_SHORT).show();
                            }

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

                    }
                });

    }

    public void ContenedorVacio(){

        if (CargoTipoMovimiento.equalsIgnoreCase("true")){CargoTipoMovimiento = "1";
        } else {CargoTipoMovimiento = "2";}

        if (Placa==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (Dni==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sCodigoContenedor==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sNumeroPrecinto==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sOrigen==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sNumero==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (stamañoContenedor==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (Delantera==null){Toast.makeText(mContext, "Falta Foto Delantera", Toast.LENGTH_SHORT).show();
            return;}

        if (Trasera==null){Toast.makeText(mContext, "Flata Foto Trasera", Toast.LENGTH_SHORT).show();
            return;}

        if (Panoramica==null){Toast.makeText(mContext, "Falta Foto Panoramica", Toast.LENGTH_SHORT).show();
            return;}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contadorLista!=Integer.valueOf(sNumeroPrecinto)){Toast.makeText(mContext, "Complete foto(s) de Precinto(s)", Toast.LENGTH_SHORT).show();
            return;}


        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/CreateContenedorVacio");

        List <Part> files = new ArrayList();

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            if (c.moveToFirst()) {
                do {
                    files.add(new FilePart("Precintos", new File(c.getString(c.getColumnIndex("Foto")))));
                } while (c.moveToNext());
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {


                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
                .addMultipartParts(files)
                .setMultipartParameter("Numero", Numero)
                .setMultipartParameter("DispositivoId", DispositivoId)
                .setMultipartParameter("CargoTipoMovimientoId", CargoTipoMovimiento)
                .setMultipartParameter("Placa", Placa)
                .setMultipartParameter("NroDOI", Dni)
                .setMultipartParameter("VigenciaLicenciaConducir", Licencia)
                .setMultipartParameter("TamanoContenedor", stamañoContenedor)
                .setMultipartParameter("CargoTipoCargaId", TipoCarga)
                .setMultipartParameter("CodigoContenedor", sCodigoContenedor)
                .setMultipartParameter("NumeroPrecintos", sNumeroPrecinto)
                .setMultipartParameter("OrigDest", sOrigen)
                .setMultipartParameter("ContenedorTipoDocumentoId", sTipoDocumento)
                .setMultipartParameter("NroORGR", sNumero)
                .setMultipartParameter("Casco", Casco)
                .setMultipartParameter("Chaleco", Chaleco)
                .setMultipartParameter("Botas", Botas)
                .setMultipartFile("Delantera", new File(Delantera))
                .setMultipartFile("Trasera", new File(Trasera))
                .setMultipartFile("Interior", new File(Panoramica))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if (e != null){

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
                            }

                            return;
                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            if (result.get("Estado").getAsBoolean()){

                                limpiarDatos();

                                try {
                                    showDialogSend();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {

                                Toast.makeText(mContext, result.get("Exception").getAsString(), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                        }
                    }
                });
    }

    public void ContenedorLLeno(){

        if (CargoTipoMovimiento.equalsIgnoreCase("true")){CargoTipoMovimiento = "1";
        } else {CargoTipoMovimiento = "2";}

        if (Placa==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (Dni==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sCodigoContenedor==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sNumeroPrecinto==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sOrigen==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (sNumero==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (stamañoContenedor==null){Toast.makeText(mContext, "Datos Incompletos", Toast.LENGTH_SHORT).show();
            return;}

        if (Delantera==null){Toast.makeText(mContext, "Falta Foto Delantera", Toast.LENGTH_SHORT).show();
            return;}

        if (Trasera==null){Toast.makeText(mContext, "Falta Foto Trasera", Toast.LENGTH_SHORT).show();
            return;}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contadorLista!=Integer.valueOf(sNumeroPrecinto)){Toast.makeText(mContext, "Falta Foto de Precinto", Toast.LENGTH_SHORT).show();
            return;}

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/CreateContenedorLLeno");

        List <Part> files = new ArrayList();


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            if (c.moveToFirst()) {

                do {
                    files.add(new FilePart("Precintos", new File(c.getString(c.getColumnIndex("Foto")))));
                } while (c.moveToNext());

            }

            c.close();
            dbst.close();

        } catch (Exception e) {}

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
                .addMultipartParts(files)
                .setMultipartParameter("Numero", Numero)
                .setMultipartParameter("DispositivoId", DispositivoId)
                .setMultipartParameter("CargoTipoMovimientoId", CargoTipoMovimiento)
                .setMultipartParameter("Placa", Placa)
                .setMultipartParameter("NroDOI", Dni)
                .setMultipartParameter("VigenciaLicenciaConducir", Licencia)
                .setMultipartParameter("TamanoContenedor", stamañoContenedor)
                .setMultipartParameter("CargoTipoCargaId", TipoCarga)
                .setMultipartParameter("CodigoContenedor", sCodigoContenedor)
                .setMultipartParameter("NumeroPrecintos", sNumeroPrecinto)
                .setMultipartParameter("OrigDest", sOrigen)
                .setMultipartParameter("ContenedorTipoDocumentoId", sTipoDocumento)
                .setMultipartParameter("NroORGR", sNumero)
                .setMultipartParameter("Casco", Casco)
                .setMultipartParameter("Chaleco", Chaleco)
                .setMultipartParameter("Botas", Botas)
                .setMultipartFile("Delantera", new File(Delantera))
                .setMultipartFile("Trasera", new File(Trasera))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if (e != null){

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
                            }

                            return;
                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject ", result.toString());

                            if (result.get("Estado").getAsBoolean()){

                                limpiarDatos();
                                try {
                                    showDialogSend();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {

                                Toast.makeText(mContext, result.get("Exception").getAsString(), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                        }
                    }
                });
    }

    public void loadPrecinto(){

        int count = 0;

        String numeroPrecintos = null;

            dataModelsMovil = new ArrayList<>();

            listView = (GridView) viewPager.findViewById(R.id.quinto_list_fotos);


        if (listView==null){
            return;
        }
            try {
                DBHelper dataBaseHelper = new DBHelper(this);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT Foto, Indice FROM CargoPrecinto";
                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {

                    do {
                        dataModelsMovil.add(new PrecintoDataModel(c.getString(c.getColumnIndex("Indice")), c.getString(c.getColumnIndex("Foto"))));
                    } while (c.moveToNext());

                }
                c.close();
                dbst.close();

            } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
        } catch (Exception e) {}


        adapterMovil= new PrecintoCustomAdapter(dataModelsMovil,getApplicationContext());
        listView.setAdapter(adapterMovil);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PrecintoDataModel datamo = dataModelsMovil.get(position);

                fotoA = false;
                fotoB = false;
                fotoC = false;
                fotoPrecinto = true;

                if (datamo.getFoto()==null){
                    fotoPrecinto(datamo.getNum() );
                } else {
                    visualizarImagen(datamo.getFoto());
                }
            }
        });

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, numeroPrecintos FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                numeroPrecintos = c.getString(c.getColumnIndex("numeroPrecintos"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        quinto_txt_nro_precintos.setText("Precintos: "+ String.valueOf(contadorLista) +" de "+ numeroPrecintos);

    }

    @Override
    public void onBackPressed() {
        mensajeSalir();
    }

    public void mensajeSalir(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Está seguro que desea salir?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                limpiarDatos();
                dialog.dismiss();
                finish();
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

    public boolean limpiarDatos(){

        try {

            DBHelper dbHelperAlarm = new DBHelper(this);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Cargo SET NroOR = " + null);
            dba.execSQL("UPDATE Cargo SET json = " + null);
            dba.execSQL("UPDATE Cargo SET fotoDelantera = " + null);
            dba.execSQL("UPDATE Cargo SET fotoTracera = " + null);
            dba.execSQL("UPDATE Cargo SET fotoPanoramica = " + null);
            dba.execSQL("UPDATE Cargo SET Placa = " + null);
            dba.execSQL("UPDATE Cargo SET Dni = " + null);
            dba.execSQL("UPDATE Cargo SET CantidadBultos = " + null);
            dba.execSQL("UPDATE Cargo SET codigoContenedor = " + null);
            dba.execSQL("UPDATE Cargo SET numeroPrecintos = " + null);
            dba.execSQL("UPDATE Cargo SET origenDestino = " + null);
            dba.execSQL("UPDATE Cargo SET numeroDocumento = " + null);
            dba.execSQL("UPDATE Cargo SET GuiaRemision = " + null);

            dba.close();

        } catch (Exception eew){
            Log.e("Exception limpiarDatos ", eew.getMessage());
        }

        limpiarDatosRadioBoton();

        DBHelper dbgelperDeete = new DBHelper(this);
        SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM CargoPrecinto");
        sqldbDelete.close();

        return true;
    }

    public boolean limpiarDatosRadioBoton(){

        try {
            DBHelper dbHelperNumero = new DBHelper(mContext);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            dbNro.execSQL("UPDATE Cargo SET TipoCarga = '1' WHERE CargoId = 1");
            dbNro.execSQL("UPDATE Cargo SET EppCasco = 'false'");
            dbNro.execSQL("UPDATE Cargo SET EppChaleco = 'false'");
            dbNro.execSQL("UPDATE Cargo SET EppBotas = 'false'");
            dbNro.execSQL("UPDATE Cargo SET isCarga = 'false'");
            dbNro.execSQL("UPDATE Cargo SET isLicencia = 'true'");
            dbNro.close();
        } catch (Exception eew){}


        return true;
    }

    public boolean ejecutarApiRegistro(String placaR, String GuidDipositivo){



        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoActivity.this);
        pDialog.setMessage("Registrando Placa...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        String URL = URL_API.concat("api/Cargo/RegistrarPlaca");

        JsonObject json = new JsonObject();
        json.addProperty("DispositivoId", GuidDipositivo);
        json.addProperty("Placa", placaR);
        json.addProperty("CodigoEmpresa", "1");
        json.addProperty("CodigoTipoVehiculo", "6");

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if(response == null){

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception exc){
                                return;
                            }
                        }

                        if (response.getHeaders().code() == 200) {
                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject Registro ", "Placa " +result.toString());

                            try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                            } catch (Exception exc){}

                            enviarPlaca();
                            //finish();


                        } else {
                            try {
                                if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                            } catch (Exception exc){}

                            Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                        } catch (Exception exc){}
                    }
                });

        return  true;
    }

    public void mensajePersona(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Persona no encontrada!. No se ha obtenido datos de ésta persona.");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }

    public void MensajeError(String title, String mensaje){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_dni_patrol_failed, null);
        mBuilder.setCancelable(false);

        TextView txtTitle = (TextView) mView.findViewById(R.id.cargo_title_failed);
        TextView texMje = (TextView)mView.findViewById(R.id.cargo_mje_failed);

        txtTitle.setText("¡"+title+"!");
        texMje.setText(mensaje);

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    try {

                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Cargo SET Dni = " + null);
                        dba.close();

                    } catch (Exception eew){}

                    primero_edt_dni.setText("");
                    primero_edt_dni.setFocusable(true);

                    dialog.dismiss();

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void MensajePersona(String mensaje) {

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_dni_patrol_ok, null);

        TextView txtTitle = (TextView) mView.findViewById(R.id.cargo_title_failed);
        TextView texMje = (TextView) mView.findViewById(R.id.cargo_mje_failed);

        txtTitle.setText("¡INFORMACIÓN!");
        texMje.setText(mensaje);

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    /*limpiarDatosRadioBoton();
                    limpiarDatos();
                    limpiarDatosPlaca();*/

                    dialog.dismiss();

                    /*Intent intent = getIntent();
                    finish();
                    startActivity(intent);*/

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void visualizarImagen(String uri){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);
        mView = getLayoutInflater().inflate(R.layout.popup_visualizacion, null);
        mBuilder.setCancelable(false);

        ImageView img = (ImageView) mView.findViewById(R.id.popup_img_visualizacion);
        Uri myUri = Uri.parse(getRightAngleImage(uri));
        img.setImageURI(myUri);

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            });
            mBuilder.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (fotoPrecinto){

                        try {
                            DBHelper dataBaseHelperB = new DBHelper(mContext);
                            SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                            //dbU.execSQL("DELETE FROM CargoPrecinto WHERE Foto = '"+uri+"'");
                            dbU.execSQL("UPDATE CargoPrecinto SET Foto = " + null+" WHERE Foto = '"+uri+"'");
                            dbU.close();

                        } catch (Exception e){}

                    } else {

                        try {

                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();

                            if (fotoA){
                                dba.execSQL("UPDATE Cargo SET fotoDelantera = " + null);
                                btn_visualizar_delantera.setVisibility(View.GONE);
                            } else if (fotoB){
                                dba.execSQL("UPDATE Cargo SET fotoTracera = " + null);
                                btn_visualizar_trasera.setVisibility(View.GONE);
                            } else if (fotoC){
                                dba.execSQL("UPDATE Cargo SET fotoPanoramica = " + null);
                                btn_visualizar_panoramica.setVisibility(View.GONE);
                            }

                            dba.close();

                        } catch (Exception eew){}
                    }


                    loadPrecinto();

                    dialog.dismiss();

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void previewFotoCargo(View view){

        String json = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT json FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                json = c.getString(c.getColumnIndex("json"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        if (json==null){

            Toast.makeText(mContext, "¡No existe foto!", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        JsonObject result = gson.fromJson(json, JsonObject.class);

        if (!result.get("Img").isJsonNull()){
            visualizarImagenX(result.get("Img").getAsString());
        }

    }

    public void visualizarImagenX(String uri){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);
        mView = getLayoutInflater().inflate(R.layout.popup_visualizacion, null);
        mBuilder.setCancelable(false);

        ImageView img = (ImageView) mView.findViewById(R.id.popup_img_visualizacion);

        try {
            Ion.with(img)
                    .placeholder(R.drawable.ic_foto_fail)
                    .error(R.drawable.ic_foto_fail)
                    .load(uri);
        } catch (Exception dsf){}


        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getRightAngleImage(String photoPath) {

        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 90;
            }

            return rotateImage(degree,photoPath);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return photoPath;
    }

    private String rotateImage(int degree, String imagePath){

        if(degree<=0){
            return imagePath;
        }
        try{
            Bitmap b= BitmapFactory.decodeFile(imagePath);

            Matrix matrix = new Matrix();
            if(b.getWidth()>b.getHeight()){
                matrix.setRotate(degree);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                        matrix, true);
            }

            FileOutputStream fOut = new FileOutputStream(imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

            FileOutputStream out = new FileOutputStream(imagePath);
            if (imageType.equalsIgnoreCase("png")) {
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
            }else if (imageType.equalsIgnoreCase("jpeg")|| imageType.equalsIgnoreCase("jpg")) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            fOut.flush();
            fOut.close();

            b.recycle();
        }catch (Exception e){
            e.printStackTrace();
        }
        return imagePath;
    }

    public void visualizacionDelantera(View view){

        String fotoD = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT fotoDelantera FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                fotoD = c.getString(c.getColumnIndex("fotoDelantera"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        fotoA = true;
        fotoB = false;
        fotoC = false;
        fotoPrecinto = false;
        visualizarImagen(fotoD);

    }

    public void visualizacionTrasera(View view){

        String fotoT = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT fotoTracera FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                fotoT = c.getString(c.getColumnIndex("fotoTracera"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        fotoA = false;
        fotoB = true;
        fotoC = false;
        fotoPrecinto = false;
        visualizarImagen(fotoT);

    }

    public void visualizacionPanoramica(View view){

        String fotoP = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT fotoPanoramica FROM Cargo";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                fotoP = c.getString(c.getColumnIndex("fotoPanoramica"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        fotoA = false;
        fotoB = false;
        fotoC = true;
        fotoPrecinto = false;
        visualizarImagen(fotoP);

    }

    public void mensajeTimeOut(){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoActivity.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_dni_patrol_failed, null);
        mBuilder.setCancelable(false);

        TextView txtTitle = (TextView) mView.findViewById(R.id.cargo_title_failed);
        TextView texMje = (TextView)mView.findViewById(R.id.cargo_mje_failed);

        txtTitle.setText("¡Atención!");
        texMje.setText("Lo sentimos, el servidor ha demorado en responder. " +
                "Intente nuevamente en un momento, caso contrario pongase en contacto con su administrador");

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
