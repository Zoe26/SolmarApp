package com.idslatam.solmar.Cargo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Cargo.Foto.CargoFotoCustomAdapter;
import com.idslatam.solmar.Cargo.Foto.CargoFotoDataModel;
import com.idslatam.solmar.Cargo.Precinto.PrecintoCustomAdapter;
import com.idslatam.solmar.Cargo.Precinto.PrecintoDataModel;
import com.idslatam.solmar.Cargo.TipoCarga.TipoCargaCustomPagerAdapter;
import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Crud.CargoFormFotoCrud;
import com.idslatam.solmar.Models.Crud.CargoPrecintoCrud;
import com.idslatam.solmar.Models.Crud.CargoTipoFotoCrud;
import com.idslatam.solmar.Models.Crud.ConfiguracionCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.CargoFormFoto;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;
import com.idslatam.solmar.Models.Entities.CargoTipoFoto;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoAlcolimetroDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoAutenticacionDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoCargaFormDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoCargaFormDataDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoFormTakeFotoAsync;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoFotoFormDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoFotoFormDataDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoPersonaFormDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoRutaFormDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoRutaFormItemDTO;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoTipoCargaDTO;
import com.idslatam.solmar.Models.Entities.DTO.Configuracion.ConfiguracionSingleDTO;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CargoFormActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    ViewPager vpCargoForm;
    Context mContext;
    TipoCargaCustomPagerAdapter adapterTipoCarga = null;
    PrecintoCustomAdapter adapterMovil;
    CargoFotoCustomAdapter cargoFotoCustomAdapter;
    Boolean gridViewResized = false, btn_dni = false,isScanner = false,btn_nroDoc=false,gvTipoFotoResized=false
            ,gvPrecintoResized=false,takeHeightTipoCarga=false,loadRuta=false,firstLoadOrigen=true,
            firstLoadDestino = true,viewOrigen = false,viewDestino= false,reloadCarga=false;
    GridView gvTipoCarga,gvPrecintos,gvCargoFotos;
    int sizeTipoCarga = 0,_CargoPrecinto_Id=0,heightTipoFoto=0,heightFotoPrecinto=0,columnsFotoPrecinto=0,
            heightTipoCarga=0,fotoPrecinto=0,TIME_OUT = 5 * 60 * 1000,columnsCargaFoto=0;
    String urlApi,valor,formato,numeroPrecintoFoto = "0",imageFilePath="",CodigoSincronizacion="";
    EditText primero_edt_tracto,primero_edt_dni,cuarto_edt_codContenedor,cuarto_edt_or,
            etxtCarreta,etxtCantidadBultos,etxtPV,etxtNroPrecintos;
    LinearLayout lnLyTamanioContenedor,lnLyOrigen,lnLyDestino,lnLyGuiaTicket,lnLyCarreta,lnLyCarga,
            lnLyNroDocumento,lnLyPV,lnLyCodigoContenedor,lnLyPrecintos,lnLyBultos,lnLyCargaVerificada,
            lnlyTempOrder,lnlyAlcolimetro;
    CheckBox chBoxCarga,chBoxCargaVerificada;
    TextView primero_txt_mje,cargo_txt_dni_persona, cargo_txt_empresa_persona,
            txtTamanioContenedor,txtOrigen,txtDestino,quinto_txt_nro_precintos,
            txtIngresoTracto,txtCarga,txtDni,txtCarreta,txtTieneCarga,txtNroDocumento,txtPV,
            txtCodigoContenedor,txtPrecintos,txtBultos,txtCargaVerificada,
            quinto_txt_dni, quinto_txt_carga,quinto_txt_ingreso_tracto,txtPAPositivo,txtPANegativo;
    Spinner spinOrigen,spinDestino;
    Button btnEscanear,primero_btn_verificar,primero_btn_carga
            //btn Carga
            ,cuarto_btn_persona,cuarto_btn_foto
            ,cuarto_btn_personas,cuarto_btn_fotos
            //btn Finalizar
            ,btn_finish_carga,btn_finish_cargas,btn_finish;
    ImageView img_cargo_persona;
    SwitchCompat isLicencia,switchTamanoContenedor,switchTipoGuiaBalance,switchResultadoAlcoholimetro;
    CheckBox check_casco,check_chaleco,check_botas,chBoxPruebaAlcohol;

    CargoCrud cargoCrud;
    ConfiguracionCrud configuracionCrud;
    Calendar currenCodeBar;
    CargoPrecintoCrud cargoPrecintoCrud;
    CargoTipoFotoCrud cargoTipoFotoCrud;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
    CargoCargaFormDTO cargoCargaForm = new CargoCargaFormDTO();
    CargoFotoFormDTO cargoFotoFormDTO = new CargoFotoFormDTO();
    CargoFotoDataModel cargoFotoDataModel = null;
    CargoFormFotoCrud cargoFormFotoCrud;

    ArrayList<String> dataOrigen,dataDestino;
    ArrayList<PrecintoDataModel> dataModelsMovil;
    ArrayList<CargoFotoDataModel> cargoFotoDataModels;
    ArrayList<CargoRutaFormItemDTO> DataOrigenes;
    ArrayList<CargoRutaFormItemDTO> DataDestinos;
    CargoRutaFormItemDTO selectedOrigen = new CargoRutaFormItemDTO();
    CargoRutaFormItemDTO selectedDestino = new CargoRutaFormItemDTO();
    List<CargoFormFoto> cargoFormFotos = new ArrayList<>();

    ArrayList<CargoAutenticacionDTO> cargoAutenticacionDTO;
    CargoAlcolimetroDTO cargoAlcolimetroDTO = new CargoAlcolimetroDTO();

    private static final int REQUEST_CODE_PHOTO_TAKEN_ASYNC = 2;

    public void finalizarCargoSend(View view){

        //Validación de fotos
        int fotosPrecintoTotal =  cargoPrecintoCrud.fotosTotal();
        int fotosPrecintoTomadas =  cargoPrecintoCrud.fotosTomadas();

        int fotosTipoTotal =  cargoTipoFotoCrud.getFotosTotal();
        int fotosTipoTomadas =  cargoTipoFotoCrud.getFotosTomadas();

        if(fotosPrecintoTotal != fotosPrecintoTomadas){
            Toast.makeText(mContext, "Complete fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        if(fotosTipoTotal != fotosTipoTomadas){
            Toast.makeText(mContext, "Complete fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        Cargo cargo = cargoCrud.getCargo();
        ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoFormActivity.this);
        pDialog.setMessage("Registrando Cargo...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = urlApi.concat("api/CargoForms/CreateAsync");
        CodigoSincronizacion = UUID.randomUUID().toString();

        cargoFormFotos = cargoFormFotoCrud.getListForServer(CodigoSincronizacion);

        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("CodigoSincronizacion", Arrays.asList(CodigoSincronizacion));

        //Objeto Persona:
        String Placa = cargo.getPlaca();
        //String IsIngreso = cargo.getIsIngreso();
        String CargoTipoMovimiento = cargo.getIsIngreso().equalsIgnoreCase("true") ? "1":"2";
        String TipoCarga = cargo.getTipoCarga();
        String Casco = cargo.getEppCasco()==null?"false":cargo.getEppCasco();
        String Chaleco = cargo.getEppChaleco()==null?"false":cargo.getEppChaleco();
        String Botas = cargo.getEppBotas()==null?"false":cargo.getEppBotas();
        String Dni = cargo.getDni();
        String Licencia = cargo.getIsLicencia()==null?"false":cargo.getIsLicencia();

        //String NroPrecinto = null;
        params.put("Placa", Arrays.asList(Placa));
        params.put("CargoTipoMovimientoId", Arrays.asList(CargoTipoMovimiento));
        params.put("ClienteCargaId", Arrays.asList(TipoCarga));
        params.put("Casco", Arrays.asList(Casco));
        params.put("Chaleco", Arrays.asList(Chaleco));
        params.put("Botas", Arrays.asList(Botas));
        params.put("NroDoi", Arrays.asList(Dni));
        params.put("VigenciaLicenciaCoducir", Arrays.asList(Licencia));

        if(cargo.getAlcolimetro()!=null){
            params.put("PruebaAlcolimetro", Arrays.asList(cargo.getAlcolimetro()));
        }

        for (CargoCargaFormDataDTO item : cargoCargaForm.Data) {
            switch (item.Codigo){
                case "01":
                    break;
                case "02":
                    break;
                case "03":
                    if(cargo.getNumeroPrecintos() !=null && !cargo.getNumeroPrecintos().isEmpty()){
                        //NroPrecinto = cargo.getNumeroPrecintos();
                        params.put("NroPrecintos", Arrays.asList(cargo.getNumeroPrecintos()));
                    }
                    break;
                case "04"://Carreta
                    if(item.Visible){
                        if(cargo.getCarreta() !=null && !cargo.getCarreta().isEmpty()){
                            params.put("Carreta", Arrays.asList(cargo.getCarreta()));
                        }
                    }
                    break;
                case "05"://Carga
                    if(item.Visible){
                        if(cargo.getIsCarga()!=null && !cargo.getIsCarga().isEmpty()){

                            params.put("IsCarga", Arrays.asList(cargo.getIsCarga()));
                            if(cargo.getIsCarga().equalsIgnoreCase("true")){

                                if (cargo.getIsIngreso().equalsIgnoreCase("true")){
                                    if(viewOrigen){
                                        params.put("OrigenId", Arrays.asList(cargo.getOrigenId()));
                                    }
                                } else {
                                    if(viewDestino){
                                        params.put("DestinoId", Arrays.asList(cargo.getDestinoId()));
                                    }

                                }
                            }

                        }
                    }
                    break;
                case "06"://Nro Documento
                    if(item.Visible){
                        if(cargo.getNumeroDocumento() !=null && !cargo.getNumeroDocumento().isEmpty()){
                            params.put("NroDocumento", Arrays.asList(cargo.getNumeroDocumento()));
                        }
                    }
                    break;
                case "08"://PV
                    if(item.Visible){
                        if(cargo.getPv() !=null && !cargo.getPv().isEmpty()){
                            params.put("Pv", Arrays.asList(cargo.getPv()));
                        }
                    }
                    break;
                case "09"://Cantidad de Bultos
                    if(item.Visible){
                        if(cargo.getCantidadBultos() !=null && !cargo.getCantidadBultos().isEmpty()){
                            params.put("CantidadBultos", Arrays.asList(cargo.getCantidadBultos()));
                        }
                    }
                    break;
                case "10"://Carga Verificada
                    if(item.Visible){
                        if(cargo.getIsCargaVerificada()!=null && !cargo.getIsCargaVerificada().isEmpty()){
                            params.put("CargaVerificada", Arrays.asList(cargo.getIsCargaVerificada()));
                        }
                    }
                    break;
                case "11"://Codigo Contenedor
                    if(item.Visible){
                        if(cargo.getCodigoContenedor() !=null && !cargo.getCodigoContenedor().isEmpty()){
                            params.put("CodigoContenedor", Arrays.asList(cargo.getCodigoContenedor()));
                        }
                    }
                    break;
                case "12"://Tamaño Contenedor
                    if(item.Visible){
                        if(cargo.getTamanoContenedor()!=null && !cargo.getTamanoContenedor().isEmpty()){
                            params.put("TamannoContenedor", Arrays.asList(cargo.getTamanoContenedor()));
                        }
                    }
                    break;
                case "13":
                    if(item.Visible){
                        if(cargo.getGuiaRemision()!=null && !cargo.getGuiaRemision().isEmpty()){
                            params.put("GuiaRemision", Arrays.asList(cargo.getGuiaRemision()));
                        }
                    }
                    break;
            }
        }

        //String Numero = config.Numero;
        String DispositivoId = config.DispositivoId;

        params.put("DispositivoId", Arrays.asList(DispositivoId));

        Log.e("Params",params.toString());

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
                .setBodyParameters(params)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        //Eliminar
                        /*
                        pDialog.dismiss();

                        if(e!=null){
                            Log.e("Exception x",e.toString());
                        }
                        if(response!=null){
                            Log.e("Response ",response.toString());

                            if(response.getHeaders().code()==200){
                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                Log.e("JsonObject ", result.toString());
                            }
                        }
                        */

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

                                enviarFotosSingle(cargoFormFotos);
                                //Activar
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
                        else{
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                        }

                    }
                });



    }

    public void enviarFotosSingle(List<CargoFormFoto> cargoFormFotos){
        new sendPhotoAsync().execute(cargoFormFotos);
    }

    private class sendPhotoAsync extends AsyncTask<List<CargoFormFoto>, Void, List<CargoFormFoto>> {

        @Override
        protected List<CargoFormFoto> doInBackground(List<CargoFormFoto>... params) {

            List<CargoFormFoto> objFotoWork = params[0];
            CargoFormFotoCrud cargoFotoCrud = new CargoFormFotoCrud(mContext);
            ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

            for (CargoFormFoto cargoFoto : objFotoWork) {

                File archivoFoto = new File(cargoFoto.filePath);

                if(archivoFoto.exists()){

                    Map<String, List<String>> paramsMP = new HashMap<String, List<String>>();
                    paramsMP.put("DispositivoId", Arrays.asList(config.DispositivoId));
                    paramsMP.put("CodigoSincronizacion", Arrays.asList(cargoFoto.codigoSincronizacion));
                    paramsMP.put("Indice", Arrays.asList(cargoFoto.indice));

                    if(cargoFoto.clienteCargaFotoId != null){
                        paramsMP.put("ClienteCargaFotoId", Arrays.asList(cargoFoto.clienteCargaFotoId));
                    }

                    //
                    String URL = urlApi.concat("api/CargoForms/SincronizacionFoto");

                    Ion.with(mContext)
                            .load(URL)
                            .uploadProgressHandler(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    Log.e("total = " + String.valueOf((int) total), "--- uploaded Fotos= " + String.valueOf(uploaded));
                                }
                            })
                            .setTimeout(TIME_OUT)
                            .setMultipartParameters(paramsMP)
                            .setMultipartFile("file", new File(cargoFoto.filePath))
                            .asString()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<String>>() {
                                @Override
                                public void onCompleted(Exception e, Response<String> response) {

                                    if(response!=null){
                                        Log.e("Response ff",response.toString());

                                        //if(response.getHeaders().code()==200){
                                            Gson gson = new Gson();
                                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                            Log.e("JsonObject ", result.toString());
                                        //}
                                    }

                                    if(response.getHeaders().code()==200){

                                        Gson gson = new Gson();
                                        JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                        Log.e("JsonObject ", result.toString());

                                        if (result.get("Estado").getAsBoolean()){

                                            cargoFormFotoCrud.removeCargoFoto(cargoFoto);

                                            File file = new File(cargoFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    cargoFormFotoCrud.removeCargoFoto(cargoFoto);
                }


            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<CargoFormFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;

        Constants constants = new Constants();
        urlApi = constants.getURL();

        cargoCrud = new CargoCrud(mContext);
        configuracionCrud = new ConfiguracionCrud(mContext);
        cargoPrecintoCrud = new CargoPrecintoCrud(mContext);
        cargoTipoFotoCrud = new CargoTipoFotoCrud(mContext);
        cargoFormFotoCrud = new CargoFormFotoCrud(mContext);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargo_form);

        vpCargoForm = (ViewPager) findViewById(R.id.vpCargoForm);
        vpCargoForm.setAdapter(new CustomPagerAdapter(this));
        vpCargoForm.addOnPageChangeListener(this);


        /*
        vpCargoForm.beginFakeDrag();


        vpCargoForm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        */

        cargoCargaForm = null;
        dataOrigen = new ArrayList<>();
        dataDestino = new ArrayList<>();
        DataOrigenes = new ArrayList<>();
        DataDestinos = new ArrayList<>();
        firstLoadOrigen = true;

        loadRuta = false;

        final File newFile = new File(Environment.getExternalStorageDirectory() + "/SOLGIS/Cargo");
        //Environment.getExternalStorageDirectory()+"/SOLGIS/Cargo"
        newFile.mkdirs();
        Log.e("Direx",newFile.getAbsolutePath().toString());

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("onPageSelected",String.valueOf(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.e("onPageScrollState",String.valueOf(state));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        try{
            switch (position){
                case 0:
                    //Vista Persona:
                    primero_txt_mje = (TextView) findViewById(R.id.primero_txt_mje);
                    primero_edt_tracto = (EditText)findViewById(R.id.primero_edt_tracto);

                    primero_edt_dni = (EditText)findViewById(R.id.primero_edt_dni);
                    img_cargo_persona = (ImageView)vpCargoForm.findViewById(R.id.img_cargo_persona);
                    cargo_txt_dni_persona = (TextView)vpCargoForm.findViewById(R.id.cargo_txt_dni_persona);
                    cargo_txt_empresa_persona = (TextView)vpCargoForm.findViewById(R.id.cargo_txt_empresa_persona);
                    gvTipoCarga = (GridView) vpCargoForm.findViewById(R.id.gvTipoCarga);
                    check_casco = (CheckBox) vpCargoForm.findViewById(R.id.check_casco);
                    check_chaleco = (CheckBox) vpCargoForm.findViewById(R.id.check_chaleco);
                    check_botas = (CheckBox) vpCargoForm.findViewById(R.id.check_botas);
                    chBoxPruebaAlcohol = (CheckBox)vpCargoForm.findViewById(R.id.chBoxPruebaAlcohol);
                    lnlyAlcolimetro = (LinearLayout) vpCargoForm.findViewById(R.id.lnlyAlcolimetro);
                    txtPAPositivo = (TextView)vpCargoForm.findViewById(R.id.txtPAPositivo);
                    txtPANegativo = (TextView)vpCargoForm.findViewById(R.id.txtPANegativo);
                    primero_btn_verificar = (Button)vpCargoForm.findViewById(R.id.primero_btn_verificar);
                    primero_btn_carga = (Button)vpCargoForm.findViewById(R.id.primero_btn_carga);

                    getPersonaForm(urlApi);

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

                    switchResultadoAlcoholimetro = (SwitchCompat)findViewById(R.id.switchResultadoAlcoholimetro);
                    switchResultadoAlcoholimetro.setClickable(false);
                    switchResultadoAlcoholimetro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            Log.e("Change ResAl",String.valueOf(isChecked));

                            if (isChecked){
                                try {
                                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                    dba.execSQL("UPDATE Cargo SET Alcolimetro = 'false'");
                                    dba.close();
                                } catch (Exception eew){}

                            } else {
                                try {
                                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                    dba.execSQL("UPDATE Cargo SET Alcolimetro = 'true'");
                                    dba.close();
                                } catch (Exception eew){}
                            }
                            //Toast.makeText(ScanCode.this, "Is checked? "+swCarga.isChecked(), Toast.LENGTH_SHORT).show();

                        }
                    });


                    drawPersonaForm();
                    drawPersonaAlcolimetroForm();



                    break;
                case 1:

                    etxtCarreta = (EditText)vpCargoForm.findViewById(R.id.etxtCarreta);
                    etxtCantidadBultos = (EditText)vpCargoForm.findViewById(R.id.etxtCantidadBultos);
                    etxtPV = (EditText)vpCargoForm.findViewById(R.id.etxtPV);
                    etxtNroPrecintos = (EditText)vpCargoForm.findViewById(R.id.etxtNroPrecintos);
                    chBoxCarga = (CheckBox) vpCargoForm.findViewById(R.id.chBoxCarga);
                    chBoxCargaVerificada = (CheckBox) vpCargoForm.findViewById(R.id.chBoxCargaVerificada);

                    lnLyCarreta = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyCarreta);
                    lnLyTamanioContenedor = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyTamanioContenedor);
                    lnLyOrigen = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyOrigen);
                    lnLyDestino = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyDestino);
                    lnLyGuiaTicket = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyGuiaTicket);
                    lnLyCarga = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyCarga);
                    lnLyNroDocumento = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyNroDocumento);
                    lnLyPV = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyPV);
                    lnLyCodigoContenedor = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyCodigoContenedor);
                    lnLyPrecintos = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyPrecintos);
                    lnLyBultos = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyBultos);
                    lnLyCargaVerificada = (LinearLayout)vpCargoForm.findViewById(R.id.lnLyCargaVerificada);
                    //edt_ingresos = (LinearLayout)vpCargoForm.findViewById(R.id.edt_ingresos);
                    cuarto_btn_persona = (Button)vpCargoForm.findViewById(R.id.cuarto_btn_persona);
                    cuarto_btn_foto = (Button)vpCargoForm.findViewById(R.id.cuarto_btn_foto);
                    cuarto_btn_personas = (Button)vpCargoForm.findViewById(R.id.cuarto_btn_personas);
                    cuarto_btn_fotos = (Button)vpCargoForm.findViewById(R.id.cuarto_btn_fotos);

                    txtTamanioContenedor = (TextView) findViewById(R.id.txtTamanioContenedor);
                    txtOrigen = (TextView) findViewById(R.id.txtOrigen);
                    txtDestino = (TextView) findViewById(R.id.txtDestino);
                    txtCarreta = (TextView) findViewById(R.id.txtCarreta);
                    txtIngresoTracto = (TextView) findViewById(R.id.txtIngresoTracto);
                    txtCarga = (TextView) findViewById(R.id.txtCarga);
                    txtTieneCarga = (TextView) findViewById(R.id.txtTieneCarga);
                    txtNroDocumento = (TextView) findViewById(R.id.txtNroDocumento);
                    txtPV = (TextView) findViewById(R.id.txtPV);
                    txtCodigoContenedor = (TextView) findViewById(R.id.txtCodigoContenedor);
                    txtPrecintos = (TextView) findViewById(R.id.txtPrecintos);
                    txtBultos = (TextView) findViewById(R.id.txtBultos);
                    txtCargaVerificada = (TextView) findViewById(R.id.txtCargaVerificada);

                    cuarto_edt_codContenedor = (EditText)vpCargoForm.findViewById(R.id.cuarto_edt_codContenedor);
                    cuarto_edt_or = (EditText)vpCargoForm.findViewById(R.id.cuarto_edt_or);
                    btnEscanear = (Button)vpCargoForm.findViewById(R.id.btnEscanear);
                    spinOrigen = (Spinner) vpCargoForm.findViewById(R.id.spinOrigen);
                    spinDestino = (Spinner) vpCargoForm.findViewById(R.id.spinDestino);

                    txtDni = (TextView) findViewById(R.id.txtDni);
                    switchTamanoContenedor = (SwitchCompat) vpCargoForm.findViewById(R.id.switchTamanoContenedor);
                    switchTipoGuiaBalance = (SwitchCompat) vpCargoForm.findViewById(R.id.switchTipoGuiaBalance);
                    getLoadRuta(urlApi);
                    getCargaForm(urlApi);


                    break;
                case 2:

                    quinto_txt_dni = (TextView) findViewById(R.id.quinto_txt_dni);
                    quinto_txt_carga = (TextView) findViewById(R.id.quinto_txt_carga);
                    quinto_txt_ingreso_tracto = (TextView) findViewById(R.id.quinto_txt_ingreso_tracto);

                    btn_finish_carga = (Button)vpCargoForm.findViewById(R.id.btn_finish_carga);
                    btn_finish_cargas = (Button)vpCargoForm.findViewById(R.id.btn_finish_cargas);
                    btn_finish = (Button)vpCargoForm.findViewById(R.id.btn_finish);

                    gvPrecintos = (GridView) findViewById(R.id.gvPrecintos);
                    gvCargoFotos = (GridView) findViewById(R.id.gvCargoFotos);
                    quinto_txt_nro_precintos =(TextView) findViewById(R.id.quinto_txt_nro_precintos);
                    drawHeaderFotos();
                    getTipoFotosForm(urlApi);
                    drawFotosPrecintoForm();

                    break;

            }
        }
        catch (Exception e){

        }



    }

    public void onchBoxCargoCarga(View view){
        boolean checked = false;
        String field = "";

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.chBoxCarga:
                field = "isCarga";
                checked = ((CheckBox) view).isChecked();
                Cargo cargo = cargoCrud.getCargo();
                //Logica para ver Origen o Destino:
                if(checked){
                    if (cargo.getIsIngreso().equalsIgnoreCase("true")){
                        if(viewOrigen){
                            lnLyOrigen.setVisibility(View.VISIBLE);
                        }
                        else{
                            lnLyOrigen.setVisibility(View.GONE);
                        }
                        lnLyDestino.setVisibility(View.GONE);
                    } else {
                        if(viewDestino){
                            lnLyDestino.setVisibility(View.VISIBLE);
                        }
                        else{
                            lnLyDestino.setVisibility(View.GONE);
                        }
                        lnLyOrigen.setVisibility(View.GONE);
                    }
                }else{
                    lnLyOrigen.setVisibility(View.GONE);
                    lnLyDestino.setVisibility(View.GONE);
                }
                break;
            case R.id.chBoxCargaVerificada:
                field = "isCargaVerificada";
                checked = ((CheckBox) view).isChecked();
                break;
            case R.id.switchTamanoContenedor:
                field = "tamanoContenedor";
                checked = ((SwitchCompat) view).isChecked();
                break;
            case R.id.switchTipoGuiaBalance:
                field = "GuiaRemision";
                checked = ((SwitchCompat) view).isChecked();
                break;
        }

        if(field!=""){
            cargoCrud.updateFieldGeneric(field,String.valueOf(checked),1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("requestCode",String.valueOf(requestCode));
        Log.e("resultCode",String.valueOf(resultCode));


        if(resultCode == 0){
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                break;
            case REQUEST_CODE_PHOTO_TAKEN_ASYNC://Fotos

                if(fotoPrecinto == 1){//Viene de Precintos:

                    Log.e("Precinto", imageFilePath );
                    CargoFormTakeFotoAsync objFotoWork = new CargoFormTakeFotoAsync(0,imageFilePath,null,numeroPrecintoFoto);

                    new takePhotoAsync().execute(objFotoWork);

                }else if(fotoPrecinto ==2){
                    CargoFormTakeFotoAsync objFotoWork = new CargoFormTakeFotoAsync(1,imageFilePath,cargoFotoDataModel.ClienteCargaFotoId,"0");
                    Log.e("Foto", imageFilePath );
                    new takePhotoAsync().execute(objFotoWork);
                }

                break;

            default:

                if (resultCode != RESULT_OK){
                    btn_dni = false;
                    btn_nroDoc = false;
                    return;
                }

                if(result != null) {
                    if(result.getContents() == null) {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else {

                        if (btn_dni){
                            btn_dni = false;
                            isScanner = true;
                            valor = result.getContents();
                            formato = result.getFormatName();
                            enviarDNI();
                        }
                        else if(btn_nroDoc){
                            cuarto_edt_or.setText(result.getContents());
                            btn_nroDoc = false;
                        }
                    }
                }

                break;
        }
    }

    private class takePhotoAsync extends AsyncTask<CargoFormTakeFotoAsync, Void, CargoFormTakeFotoAsync> {

        @Override
        protected CargoFormTakeFotoAsync doInBackground(CargoFormTakeFotoAsync... params) {

            CargoFormTakeFotoAsync objFotoWork = params[0];
            Bitmap bitmapOrig =null;
            int width = 0;
            int height = 0;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            String filePath = "";
            filePath = objFotoWork.getImageFilePath();

            try{
                bitmapOrig = BitmapFactory.decodeFile(filePath);

                if(bitmapOrig == null){
                    bitmapOrig = BitmapFactory.decodeFile(filePath, options);

                    width = bitmapOrig.getWidth();
                    height = bitmapOrig.getHeight();
                }
                else{
                    width = bitmapOrig.getWidth();
                    height = bitmapOrig.getHeight();
                }
            }
            catch (Exception e){

            }

            Log.e("Acceso",objFotoWork.getImageFilePath());
            //adjust for camera orientation
            //bitmapOrig = BitmapFactory.decodeFile(objFotoWork.getImageFilePath());
            //int width = bitmapOrig.getWidth();
            //int height = bitmapOrig.getHeight();

            ExifInterface exif = null;

            try
            {
                exif = new ExifInterface(objFotoWork.getImageFilePath());
            }
            catch (IOException e)
            {
                //Error
                e.printStackTrace();
            }

            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            // the following are reverse because we are going to rotate the image 90 due to portrait pics always used
            //int newWidth = 300;
            int newHeight = 600;
            // calculate the scale
            float newWidth = (((float) newHeight) * width)/height;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            //matrix.setRotate(90);
            matrix.postRotate(rotationAngle);
            matrix.postScale(scaleWidth, scaleHeight);

            // save a scaled down Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrig, 0, 0, width, height, matrix, true);

            //File file2 = new File(Uri_Foto);
            File file2 = new File(filePath);

            try {
                FileOutputStream out = new FileOutputStream(file2);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                switch (objFotoWork.getTipoFoto()){
                    /*case 1:

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET fotoDelantera = '"+Uri_Foto+"'");
                            dba.close();
                        } catch (Exception eew){
                            Log.e("Exception ", "fotoDelantera");
                        }

                        objFotoWork.setSuccess(true);

                        break;
                    case 2:
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET fotoTracera = '"+Uri_Foto+"'");
                            dba.close();
                        } catch (Exception eew){
                            Log.e("Exception ", "fotoTracera");
                        }

                        objFotoWork.setSuccess(true);

                        break;
                    case 3:
                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET fotoPanoramica = '"+Uri_Foto+"'");
                            dba.close();
                        } catch (Exception eew){
                            Log.e("Exception ", "fotoPanoramica");
                        }

                        objFotoWork.setSuccess(true);

                        break;*/
                    case 0:

                        try {

                            DBHelper dbHelperNumero = new DBHelper(mContext);
                            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                            dbNro.execSQL("UPDATE CargoPrecinto SET Foto = '"+filePath+"' WHERE Indice = '"+objFotoWork.getIndex()+"'");
                            dbNro.close();
                        } catch (Exception eew){}

                        //revisar

                        objFotoWork.setSuccess(true);

                        //return objFotoWork;

                        break;
                    case 1:
                        try {

                            DBHelper dbHelperNumero = new DBHelper(mContext);
                            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                            dbNro.execSQL("UPDATE CargoTipoFoto SET FilePath = '"+filePath+"' WHERE ClienteCargaFotoId = '"+objFotoWork.getClienteCargaFotoId()+"'");
                            dbNro.close();

                        } catch (Exception eew){}

                        objFotoWork.setSuccess(true);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(CargoFormTakeFotoAsync result) {
            if(result.getSuccess()){
                switch (result.getTipoFoto()){
                    case 0:
                        loadPrecinto();
                        break;
                    case 1:
                        loadCargoTipoFoto();
                        break;
                }
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public boolean validaFormCarga(){
        boolean resultado = true;
        String valueInput = "";

        for (CargoCargaFormDataDTO item: cargoCargaForm.Data) {
            //Log.e("codigo", item.Codigo);
            switch (item.Codigo){
                case "01":
                    break;
                case "02":
                    break;
                case "03"://Nro Precintos
                    if(item.Visible){

                        valueInput = etxtNroPrecintos.getText().toString();

                        if(item.Requerido){

                            if(valueInput == null || valueInput.isEmpty()){

                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{

                                if(item.CaracteresTamanio != null){
                                    if(item.CaracteresTamanio != "") {
                                        int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                        if (valueInput.length() < _lengh) {
                                            Toast.makeText(mContext, item.Label + " debe tener " + _lengh + " caracteres.", Toast.LENGTH_SHORT).show();
                                            return false;
                                        } else {
                                            cargoCrud.updateFieldGeneric("numeroPrecintos", valueInput.trim(), 1);
                                        }
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("numeroPrecintos", valueInput.trim(), 1);
                                    }

                                }else{
                                    cargoCrud.updateFieldGeneric("numeroPrecintos",valueInput.trim(),1);
                                }
                            }

                        }else{
                            cargoCrud.updateFieldGeneric("numeroPrecintos",valueInput.trim(),1);
                        }

                    }
                    else {
                        cargoCrud.updateFieldGeneric("numeroPrecintos",null,1);
                    }
                    break;
                case "04"://Carreta
                    if(item.Visible){

                        valueInput = etxtCarreta.getText().toString();

                        if(item.Requerido){
                            if(valueInput == null || valueInput.isEmpty()){
                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{

                                if(item.CaracteresTamanio != null){
                                    int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                    if(valueInput.length() < _lengh){
                                        Toast.makeText(mContext,  item.Label+" debe tener "+_lengh+" caracteres.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("Carreta",valueInput.trim(),1);
                                    }

                                }else{
                                    cargoCrud.updateFieldGeneric("Carreta",valueInput.trim(),1);
                                }

                            }

                        }
                        else{
                            cargoCrud.updateFieldGeneric("Carreta",valueInput.trim(),1);
                        }

                    }
                    else{
                        cargoCrud.updateFieldGeneric("Carreta",null,1);
                    }
                    break;
                case "05"://Carga

                    if(item.Visible){

                        Boolean valorCBC = chBoxCarga.isChecked();
                        cargoCrud.updateFieldGeneric("isCarga",String.valueOf(valorCBC),1);
                    }
                    else{
                        cargoCrud.updateFieldGeneric("isCarga",null,1);
                    }
                    break;
                case "06"://Nro Documento
                    if(item.Visible){

                        valueInput = cuarto_edt_or.getText().toString();

                        if(item.Requerido){
                            if(valueInput == null || valueInput.isEmpty()){
                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{

                                if(item.CaracteresTamanio != null){
                                    int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                    if(valueInput.length() < _lengh){
                                        Toast.makeText(mContext,  item.Label+" debe tener "+_lengh+" caracteres.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("numeroDocumento",valueInput.trim(),1);
                                    }

                                }else{
                                    cargoCrud.updateFieldGeneric("numeroDocumento",valueInput.trim(),1);
                                }
                            }
                        }
                        else{
                            cargoCrud.updateFieldGeneric("numeroDocumento",valueInput.trim(),1);
                        }
                    }
                    else{
                        cargoCrud.updateFieldGeneric("numeroDocumento",null,1);
                    }
                    break;
                //case "07"://Boton Scanear

                case "08"://PV
                    if(item.Visible){

                        valueInput = etxtPV.getText().toString();

                        if(item.Requerido){
                            if(valueInput == null || valueInput.isEmpty()){
                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{

                                if(item.CaracteresTamanio != null){
                                    int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                    if(valueInput.length() < _lengh){
                                        Toast.makeText(mContext,  item.Label+" debe tener "+_lengh+" caracteres.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("pv",valueInput.trim(),1);
                                    }

                                }else{
                                    cargoCrud.updateFieldGeneric("pv",valueInput.trim(),1);
                                }

                            }
                        }
                        else{
                            cargoCrud.updateFieldGeneric("pv",valueInput.trim(),1);
                        }



                    }
                    else{
                        cargoCrud.updateFieldGeneric("pv",null,1);
                    }
                    break;
                case "09"://Cantidad de Bultos
                    if(item.Visible){

                        valueInput = etxtCantidadBultos.getText().toString();
                        if(item.Requerido) {
                            if(valueInput == null || valueInput.isEmpty()){
                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{
                                if(item.CaracteresTamanio != null){
                                    if(item.CaracteresTamanio != ""){
                                        int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                        if(valueInput.length() < _lengh){
                                            Toast.makeText(mContext,  item.Label+" debe tener "+_lengh+" caracteres.", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }
                                        else{
                                            cargoCrud.updateFieldGeneric("CantidadBultos",valueInput.trim(),1);
                                        }
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("CantidadBultos",valueInput.trim(),1);
                                    }


                                }else{
                                    cargoCrud.updateFieldGeneric("CantidadBultos",valueInput.trim(),1);
                                }

                            }
                        }
                        else{
                            cargoCrud.updateFieldGeneric("CantidadBultos",valueInput.trim(),1);
                        }

                    }
                    else{
                        cargoCrud.updateFieldGeneric("CantidadBultos",null,1);
                    }
                    break;
                case "10"://Carga Verificada
                    if(item.Visible){
                        Boolean bcv = chBoxCargaVerificada.isChecked();
                        cargoCrud.updateFieldGeneric("isCargaVerificada",String.valueOf(bcv),1);
                    }
                    else{
                        cargoCrud.updateFieldGeneric("isCargaVerificada",null,1);
                    }
                    break;
                case "11"://Código Contenedor
                    if(item.Visible){

                        valueInput = cuarto_edt_codContenedor.getText().toString();
                        if(item.Requerido){
                            if(valueInput == null || valueInput.isEmpty()){
                                Toast.makeText(mContext, "Ingrese "+item.Label, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            else{
                                if(item.CaracteresTamanio != null){
                                    int _lengh = Integer.valueOf(item.CaracteresTamanio);
                                    if(valueInput.length() < _lengh){
                                        Toast.makeText(mContext,  item.Label+" debe tener "+_lengh+" caracteres.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    else{
                                        cargoCrud.updateFieldGeneric("codigoContenedor",valueInput.trim(),1);
                                    }

                                }else{
                                    cargoCrud.updateFieldGeneric("codigoContenedor",valueInput.trim(),1);
                                }
                            }
                        }
                        else{
                            cargoCrud.updateFieldGeneric("codigoContenedor",valueInput.trim(),1);
                        }

                    }
                    else{
                        cargoCrud.updateFieldGeneric("codigoContenedor",null,1);
                    }
                    break;
                case "12"://Tamaño contenedor
                    if(item.Visible){
                        Boolean bstc = switchTamanoContenedor.isChecked();
                        cargoCrud.updateFieldGeneric("tamanoContenedor",String.valueOf(bstc),1);
                    }
                    else{
                        cargoCrud.updateFieldGeneric("tamanoContenedor",null,1);
                    }
                    break;
                case "13"://Guia Remisión Ticket Balanza
                    if(item.Visible){
                        Boolean bstgb = switchTipoGuiaBalance.isChecked();
                        cargoCrud.updateFieldGeneric("GuiaRemision",String.valueOf(bstgb),1);
                    }
                    else{
                        cargoCrud.updateFieldGeneric("GuiaRemision",null,1);
                    }
                    break;
            }
        }

            return resultado;
    }

    public void scanDNI(View view){

        btn_dni = true;
        scanBarcode();
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

    public void scanBarcodeNroDoc(View view) {
        btn_nroDoc = true;
        new IntentIntegrator(this).initiateScan();
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    public void enviarPlaca(View view){
        enviarPlaca();
    }

    public void enviarPlaca(){
        String pla = null;

        pla = primero_edt_tracto.getText().toString();

        if (primero_edt_tracto.getText().toString().matches("")){
            Toast.makeText(mContext, "Ingrese PLaca", Toast.LENGTH_SHORT).show();
            return;
        }

        if (primero_edt_tracto.getText().length() < 6 ){
            Toast.makeText(mContext, "La placa debe tener un mínimo de 6 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        cargoCrud.updatePlaca(pla,1);
        ConfiguracionSingleDTO configuracionSingleDTO =  configuracionCrud.getConfiguracion();

        Log.e("Placa",pla);
        Log.e("DispositivoId",configuracionSingleDTO.DispositivoId);
        Log.e("Numero",configuracionSingleDTO.Numero);

        String URL = urlApi.concat("api/Cargo/VerificaPlaca?Placa="+pla+"&DispositivoId=").concat(configuracionSingleDTO.DispositivoId);

        ProgressDialog pDialog;

        pDialog = new ProgressDialog(CargoFormActivity.this);
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

                                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);

                                View mView = getLayoutInflater().inflate(R.layout.dialog_placa_failed, null);
                                TextView texMje = (TextView)mView.findViewById(R.id.mje_placa_ok);
                                texMje.setText("La placa "+primero_edt_tracto.getText().toString()+" no ha sido registrada. " +
                                        "¿Desea registar la placa?");

                                try {

                                    mBuilder.setCancelable(false);
                                    mBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            ejecutarApiRegistro(primero_edt_tracto.getText().toString(), configuracionSingleDTO.DispositivoId);

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

    //Enviar DNI
    public void enviarDNI(){

        String NumeroCel = null, CodigoEmpleado = null;

        currenCodeBar = Calendar.getInstance();
        String fecha = formatoGuardar.format(currenCodeBar.getTime());
        String tipo = "1";

        ConfiguracionSingleDTO configuracion = configuracionCrud.getConfiguracion();

        /*
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
        */

        if (formato==null){
            formato = "No Scan";
        }

        if (!isScanner){
            valor = primero_edt_dni.getText().toString();
        }

        Log.e("-------- ", "--------");
        Log.e("GuidDipositivo ", configuracion.DispositivoId);
        Log.e("NroDOI ", valor);


        String URL = urlApi.concat("api/People/VerificaDOI?NroDOI="+valor+
                "&DispositivoId=").concat(configuracion.DispositivoId);

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(CargoFormActivity.this);
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

                                    Log.e("NroDOI ", result.get("NroDOI").getAsString());

                                    primero_edt_dni.setText(result.get("NroDOI").getAsString());
                                    cargo_txt_dni_persona.setText(result.get("persNombres").getAsString());
                                    cargo_txt_empresa_persona.setText(result.get("persEmpresa").getAsString());
                                }

                                try {
                                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                    dba.execSQL("UPDATE Cargo SET Dni = '"+result.get("NroDOI").getAsString()+"'");
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

    //Funciones carga de controles:
    private void getPersonaForm(String url){

        ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

        url = url.concat("api/Cliente/ClienteCargas/Dispositivos/")
        .concat(config.DispositivoId);

        if(adapterTipoCarga!=null){
            Log.e("Tamanio",String.valueOf(adapterTipoCarga.getCount()));

            if(cargoAlcolimetroDTO != null){
                drawPersonaAlcolimetroForm();
            }

            gvTipoCarga = (GridView) vpCargoForm.findViewById(R.id.gvTipoCarga);
            gvTipoCarga.setAdapter(adapterTipoCarga);
            gridViewResized = false;

            gvTipoCarga.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!gridViewResized) {
                        gridViewResized = true;
                        resizeGridView(gvTipoCarga, sizeTipoCarga, 2);
                    }
                }
            });

            primero_btn_verificar.setVisibility(View.VISIBLE);
            primero_btn_carga.setVisibility(View.VISIBLE);

            return;
        }

        Log.e("URL",url);

        Ion.with(mContext)
                .load(url)
                //.asJsonObject()
                .as(new TypeToken<CargoPersonaFormDTO>(){})
                .setCallback(new FutureCallback<CargoPersonaFormDTO>() {
                    @Override
                    public void onCompleted(Exception e, CargoPersonaFormDTO result) {

                        if(result==null){
                            Log.e("Exception",e.toString());
                        }
                        else if(result.Estado){

                            primero_btn_verificar.setVisibility(View.VISIBLE);
                            primero_btn_carga.setVisibility(View.VISIBLE);

                            Log.e("result",result.toString());

                            sizeTipoCarga = result.Data.Carga.size();
                            adapterTipoCarga = new TipoCargaCustomPagerAdapter(mContext, result.Data.Carga);
                            // Attach the adapter to a ListView

                            gvTipoCarga = (GridView) findViewById(R.id.gvTipoCarga);
                            gvTipoCarga.setAdapter(adapterTipoCarga);

                            gvTipoCarga.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    if (!gridViewResized) {
                                        gridViewResized = true;
                                        resizeGridView(gvTipoCarga, sizeTipoCarga, 2);
                                    }
                                }
                            });

                            //Autenticación:
                            cargoAutenticacionDTO = result.Data.Autenticacion;
                            Log.e("Autenticacion", cargoAutenticacionDTO.toString() );

                            cargoAlcolimetroDTO = result.Data.Alcolimetro;

                            if(cargoAlcolimetroDTO!=null){
                                Log.e("Alcolimetro", cargoAlcolimetroDTO.toString() );
                            }
                            else{
                                Log.e("Alcolimetro", "nulo" );
                            }

                            drawPersonaAlcolimetroForm();

                        }
                        // do stuff with the result or error
                    }
                });
    }

    //Carga de Formulario Carga
    private void getCargaForm(String url){
        Log.e("TipoCarga","Executed");

        ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

        CargoTipoCargaDTO tipoCargaSelected = adapterTipoCarga.getItemX();

        url = url.concat("api/Cliente/CargaFormularios/Items/")
                .concat(tipoCargaSelected.ClienteCargaId);

        Cargo cargo = cargoCrud.getCargo();
        Boolean actualizar= false;

        if(cargo.getUpdateTipoCarga()!=null && !cargo.getUpdateTipoCarga().isEmpty()){

            if(cargo.getUpdateTipoCarga().equalsIgnoreCase("true")){
                actualizar = true;
            }
        }
        //

        if(!actualizar){
            if(cargoCargaForm != null){
                cuarto_btn_persona.setVisibility(View.VISIBLE);
                cuarto_btn_foto.setVisibility(View.VISIBLE);
                cuarto_btn_personas.setVisibility(View.VISIBLE);
                cuarto_btn_fotos.setVisibility(View.VISIBLE);
                drawCargaForm();
                return;
            }
        }

        Log.e("url Carga Form",url);

        try{

            if(tipoCargaSelected.ClienteCargaId == null){
                cuarto_btn_persona.setVisibility(View.VISIBLE);
                cuarto_btn_foto.setVisibility(View.VISIBLE);
                cuarto_btn_personas.setVisibility(View.VISIBLE);
                cuarto_btn_fotos.setVisibility(View.VISIBLE);
                return;
            }

            Ion.with(mContext)
                    .load(url)
                    //.asJsonObject()
                    .as(new TypeToken<CargoCargaFormDTO>(){})
                    .setCallback(new FutureCallback<CargoCargaFormDTO>() {
                        @Override
                        public void onCompleted(Exception e, CargoCargaFormDTO result) {

                            try{
                                if(e != null){

                                }
                                else if(result == null){

                                }
                                else if(result.Estado){

                                    cuarto_btn_persona.setVisibility(View.VISIBLE);
                                    cuarto_btn_foto.setVisibility(View.VISIBLE);
                                    cuarto_btn_personas.setVisibility(View.VISIBLE);
                                    cuarto_btn_fotos.setVisibility(View.VISIBLE);

                                    Log.e("Carga",result.Data.toString());

                                    cargoCargaForm = result;

                                    drawCargaForm();

                                }
                            }
                            catch (Exception esx){

                            }
                            // do stuff with the result or error
                        }
                    });
        }
        catch (Exception err){

        }


    }

    //Carga Fotos Cargo
    private void getTipoFotosForm(String url){

        Log.e("Fotos Form","Executed");

        ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

        CargoTipoCargaDTO tipoCargaSelected = adapterTipoCarga.getItemX();

        url = url.concat("api/Cliente/CargaFotos/Items/")
                .concat(tipoCargaSelected.ClienteCargaId);

        Cargo cargo = cargoCrud.getCargo();

        if(cargo.getTipoCargaForFotos() != null && !cargo.getTipoCargaForFotos().isEmpty() ){
            if(cargo.getTipoCarga().equalsIgnoreCase(cargo.getTipoCargaForFotos())){
                if(cargoFotoFormDTO!=null){
                    btn_finish_carga.setVisibility(View.VISIBLE);
                    btn_finish_cargas.setVisibility(View.VISIBLE);
                    btn_finish.setVisibility(View.VISIBLE);

                    loadCargoTipoFoto();
                    return;
                }

            }
        }

        Log.e("url Carga Form",url);

        Ion.with(mContext)
                .load(url)
                //.asJsonObject()
                .as(new TypeToken<CargoFotoFormDTO>(){})
                .setCallback(new FutureCallback<CargoFotoFormDTO>() {
                    @Override
                    public void onCompleted(Exception e, CargoFotoFormDTO result) {

                        if(result.Estado){
                            btn_finish_carga.setVisibility(View.VISIBLE);
                            btn_finish_cargas.setVisibility(View.VISIBLE);
                            btn_finish.setVisibility(View.VISIBLE);

                            cargoFotoFormDTO = result;
                            cargoCrud.updateFieldGeneric("TipoCargaForFotos",cargoFotoFormDTO.ClienteCargaId,1);
                            drawTipoFotosForm();

                        }
                        // do stuff with the result or error
                    }
                });

    }

    //Load Data Ruta
    private void getLoadRuta(String url){

        ConfiguracionSingleDTO config = configuracionCrud.getConfiguracion();

        url = url.concat("api/Cliente/ClienteRutas/Dispositivos/")
                .concat(config.DispositivoId);

        if(loadRuta == true){
            return;
        }

        Log.e("url Ruta Form",url);

        Ion.with(mContext)
                .load(url)
                //.asJsonObject()
                .as(new TypeToken<CargoRutaFormDTO>(){})
                .setCallback(new FutureCallback<CargoRutaFormDTO>() {
                    @Override
                    public void onCompleted(Exception e, CargoRutaFormDTO result) {

                        if(result.Estado){

                            DataOrigenes = result.Data.Origenes;
                            DataDestinos = result.Data.Destinos;
                            Cargo cargo = cargoCrud.getCargo();


                            for (CargoRutaFormItemDTO item :result.Data.Origenes) {

                                dataOrigen.add(item.Nombre);

                                if(cargo.getOrigenId() != null && !cargo.getOrigenId().isEmpty()){
                                    if(cargo.getOrigenId().equalsIgnoreCase(item.ClienteRutaId)){
                                        selectedOrigen = item;
                                    }
                                }
                            }

                            spinOrigen.setAdapter(new ArrayAdapter<String>(CargoFormActivity.this, android.R.layout.simple_spinner_dropdown_item, dataOrigen));

                            spinOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                    //Log.e("Selected",String.valueOf(position));
                                    // your code here
                                    if(firstLoadOrigen){
                                        if(position == 0 )
                                        {
                                            if(cargo.getOrigenId() == null || cargo.getOrigenId().isEmpty()){
                                                cargoCrud.updateFieldGeneric("OrigenId",DataOrigenes.get(position).ClienteRutaId,1);
                                            }
                                            else{
                                                CargoRutaFormItemDTO objeto = new CargoRutaFormItemDTO();
                                                int index = DataOrigenes.indexOf(selectedOrigen);
                                                if(index>-1){
                                                    parentView.setSelection(index);
                                                }
                                            }
                                        }

                                        firstLoadOrigen = false;

                                    }else{
                                        cargoCrud.updateFieldGeneric("OrigenId",DataOrigenes.get(position).ClienteRutaId,1);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // your code here
                                }

                            });


                            for (CargoRutaFormItemDTO item :result.Data.Destinos) {
                                //Log.e("Destino",item.Nombre);
                                dataDestino.add(item.Nombre);

                                if(cargo.getDestinoId() != null && !cargo.getDestinoId().isEmpty()){
                                    if(cargo.getDestinoId().equalsIgnoreCase(item.ClienteRutaId)){
                                        selectedDestino = item;
                                    }
                                }
                            }
                            spinDestino.setAdapter(new ArrayAdapter<String>(CargoFormActivity.this, android.R.layout.simple_spinner_dropdown_item, dataDestino));

                            spinDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                    // your code here
                                    if(firstLoadDestino){
                                        if(position == 0 )
                                        {
                                            if(cargo.getDestinoId() == null || cargo.getDestinoId().isEmpty()){
                                                cargoCrud.updateFieldGeneric("DestinoId",DataDestinos.get(position).ClienteRutaId,1);
                                            }
                                            else{
                                                int index = DataDestinos.indexOf(selectedDestino);
                                                if(index>-1){
                                                    parentView.setSelection(index);
                                                }
                                            }
                                        }

                                        firstLoadDestino = false;

                                    }else{
                                        cargoCrud.updateFieldGeneric("DestinoId",DataDestinos.get(position).ClienteRutaId,1);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // your code here
                                }

                            });

                            loadRuta = true;
                        }
                        // do stuff with the result or error
                    }
                });

    }

    public void drawHeaderFotos(){
        Cargo cargo = cargoCrud.getCargo();

        String Placa = cargo.getPlaca();

        if (cargo.getIsIngreso().equalsIgnoreCase("true")){
            quinto_txt_ingreso_tracto.setText("Ingreso de Tracto "+Placa);
        } else {
            quinto_txt_ingreso_tracto.setText("Salida de Tracto "+Placa);
        }

        CargoTipoCargaDTO cargoTipoCarga = adapterTipoCarga.getItemX();
        quinto_txt_carga.setText(cargoTipoCarga.Nombre);

        quinto_txt_dni.setText("Conductor con DNI "+ cargo.getDni());
    }

    public void drawTipoFotosForm(){

        //Validar cambio de ClienteCargaId

        if(true){
            gvTipoFotoResized = false;
            cargoTipoFotoCrud.deleteAll();

            for (CargoFotoFormDataDTO item: cargoFotoFormDTO.Data) {

                Log.e("TASK", " INSERTAR DATA");
                try {

                    //CargoPrecintoCrud cargoPrecintoCrud = new CargoPrecintoCrud(mContext);
                    CargoTipoFoto cargoTipoFoto = new CargoTipoFoto(item.Nombre,item.ClienteCargaFotoId);
                    _CargoPrecinto_Id = cargoTipoFotoCrud.insert(cargoTipoFoto);
                } catch (Exception esca) {esca.printStackTrace();}

            }
        }

        loadCargoTipoFoto();

    }

    public void loadCargoTipoFoto(){

        cargoFotoDataModels = null;
        cargoFotoDataModels = new ArrayList<>();

        gvCargoFotos = (GridView) vpCargoForm.findViewById(R.id.gvCargoFotos);

        if (gvCargoFotos==null){
            return;
        }


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT FilePath, Nombre,ClienteCargaFotoId FROM CargoTipoFoto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                do {
                    CargoFotoDataModel cargoFotoDataModel = new CargoFotoDataModel();
                    cargoFotoDataModel.foto = c.getString(c.getColumnIndex("FilePath"));
                    cargoFotoDataModel.Nombre = c.getString(c.getColumnIndex("Nombre"));
                    cargoFotoDataModel.ClienteCargaFotoId = c.getString(c.getColumnIndex("ClienteCargaFotoId"));
                    Log.e(" CREACION TIPO FOTOS ", c.getString(c.getColumnIndex("Nombre")));
                    cargoFotoDataModels.add(cargoFotoDataModel);
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        //int fotoTomadas = cargoPrecintoCrud.fotosTomadas();

        cargoFotoCustomAdapter = new CargoFotoCustomAdapter(cargoFotoDataModels,getApplicationContext());
        gvCargoFotos.setAdapter(cargoFotoCustomAdapter);

        gvCargoFotos.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!gvTipoFotoResized) {
                    gvTipoFotoResized = true;
                    resizeGridViewFotos(gvCargoFotos, cargoFotoCustomAdapter.getCount());
                }else{

                    int rows = (int)(cargoFotoCustomAdapter.getCount() / columnsCargaFoto);
                    int remainder = cargoFotoCustomAdapter.getCount() % columnsCargaFoto;
                    int newHeight = heightTipoFoto * rows;

                    if(remainder>0){
                        newHeight = newHeight+heightTipoFoto;
                    }

                    ViewGroup.LayoutParams params = gvCargoFotos.getLayoutParams();
                    params.height = newHeight;
                    gvCargoFotos.setLayoutParams(params);
                }
            }
        });

        gvCargoFotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CargoFotoDataModel datamo = cargoFotoDataModels.get(position);

                fotoPrecinto = 2;

                if (datamo.getFoto()==null){
                    fotoTipoCarga(datamo);
                } else {
                    visualizarImagen(datamo.getFoto(),2);
                }
            }
        });

    }

    public void drawFotosPrecintoForm(){
        Cargo cargo = cargoCrud.getCargo();
        //Log.e("Nro Precintos",cargo.getNumeroPrecintos());

        int fotoTomadas = cargoPrecintoCrud.fotosTomadas();
        int fotoTotal = cargoPrecintoCrud.fotosTotal();
        int nroPrecintos = 0;

        if(cargo.getNumeroPrecintos() != null && !cargo.getNumeroPrecintos().isEmpty()){
            nroPrecintos = Integer.parseInt(cargo.getNumeroPrecintos());
            quinto_txt_nro_precintos.setText("Fotos: "+fotoTomadas+" de "+ cargo.getNumeroPrecintos());

            if(fotoTotal!=nroPrecintos){

                Log.e("Siempre ingreso",cargo.getNumeroPrecintos());

                gvPrecintoResized = false;
                cargoPrecintoCrud.deleteAll();

                for (int i = 1; i <= nroPrecintos ; i++){
                    try {
                        CargoPrecintoCrud cargoPrecintoCrud = new CargoPrecintoCrud(mContext);
                        CargoPrecinto cargoPrecinto = new CargoPrecinto();
                        cargoPrecinto.Indice = "Foto "+ String.valueOf(i);
                        cargoPrecinto.CargoPrecintoId = 0;
                        _CargoPrecinto_Id = cargoPrecintoCrud.insert(cargoPrecinto);
                    } catch (Exception esca) {esca.printStackTrace();}
                }
            }
            loadPrecinto();
        }
        else{

            try {
                DBHelper dataBaseHelperB = new DBHelper(mContext);
                SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                dbU.execSQL("DELETE FROM CargoPrecinto");
                dbU.close();

            } catch (Exception e){}

            quinto_txt_nro_precintos.setText("");

        }
    }

    public void loadPrecinto(){
        Cargo cargo = cargoCrud.getCargo();

        int count = 0;

        String numeroPrecintos = null;

        dataModelsMovil = null;

        dataModelsMovil = new ArrayList<>();

        gvPrecintos = (GridView) vpCargoForm.findViewById(R.id.gvPrecintos);


        if (gvPrecintos==null){
            return;
        }


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto, Indice FROM CargoPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                do {
                    //Log.e(" CREACION PRECINTOS ", c.getString(c.getColumnIndex("Indice")));
                    dataModelsMovil.add(new PrecintoDataModel(c.getString(c.getColumnIndex("Indice")), c.getString(c.getColumnIndex("Foto"))));
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        int fotoTomadas = cargoPrecintoCrud.fotosTomadas();

        adapterMovil= new PrecintoCustomAdapter(dataModelsMovil,getApplicationContext());
        gvPrecintos.setAdapter(adapterMovil);

        gvPrecintos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PrecintoDataModel datamo = dataModelsMovil.get(position);

                if (datamo.getFoto()==null){
                    //fotoPrecinto(datamo.getNum());
                } else {
                    //visualizarImagen(datamo.getFoto());
                }
            }
        });

        numeroPrecintos = cargo.getNumeroPrecintos();


        quinto_txt_nro_precintos.setText("Fotos: "+ String.valueOf(fotoTomadas) +" de "+ numeroPrecintos);

        gvPrecintos.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (!gvPrecintoResized) {
                    gvPrecintoResized = true;
                    resizeGridViewFotosPrecintos(gvPrecintos, adapterMovil.getCount());
                }
                else{
                    //int columns = gridView.getNumColumns();
                    int rows = (int)(adapterMovil.getCount() / columnsFotoPrecinto);
                    int remainder = adapterMovil.getCount() % columnsFotoPrecinto;
                    int newHeight = heightFotoPrecinto * rows;

                    if(remainder>0){
                        newHeight = newHeight+heightFotoPrecinto;
                    }
                    ViewGroup.LayoutParams params = gvPrecintos.getLayoutParams();
                    params.height = newHeight;
                    gvPrecintos.setLayoutParams(params);
                }
            }
        });

        gvPrecintos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PrecintoDataModel datamo = dataModelsMovil.get(position);

                fotoPrecinto = 1;

                if (datamo.getFoto()==null){
                    fotoPrecinto(datamo.getNum());
                } else {
                    visualizarImagen(datamo.getFoto(),1);
                }
            }
        });

    }

    String cadenaAll = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnÑñOoPpQqRrSsTtUuVvWwXxYyZz 1234567890";

    private InputFilter createFilter(String cadenaPermitida){

        InputFilter InputFilterNroDocumento = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                boolean includesInvalidCharacter = false;
                StringBuilder stringBuilder = new StringBuilder();

                int destLength = dend - dstart + 1;
                int adjustStart = source.length() - destLength;

                for(int i=start ; i<end ; i++) {
                    char sourceChar = source.charAt(i);
                    String soux = String.valueOf(sourceChar);
                    //if(Character.isLetterOrDigit(sourceChar)) {
                    if(cadenaPermitida.toString().contains(soux)) {
                        if(i >= adjustStart)
                            stringBuilder.append(sourceChar);
                    } else
                        includesInvalidCharacter = true;
                }
                return includesInvalidCharacter ? stringBuilder : null;

            }
        };

        return InputFilterNroDocumento;
    }

    public void drawCargaForm(){

        RelativeLayout.LayoutParams params = null;
        int resIdOrder = 0;
        Boolean workPrecintos = false;
        //fnfnfnnf
        Cargo cargo = cargoCrud.getCargo();
        String valorGet = cargo.getUpdateTipoCarga();
        Boolean limpiarDatosX = Boolean.valueOf(valorGet);

        String Placa = cargo.getPlaca();

        if (cargo.getIsIngreso().equalsIgnoreCase("true")){
            txtIngresoTracto.setText("Ingreso de Tracto "+Placa);
        } else {
            txtIngresoTracto.setText("Salida de Tracto "+Placa);
        }

        CargoTipoCargaDTO cargoTipoCarga = adapterTipoCarga.getItemX();
        txtCarga.setText(cargoTipoCarga.Nombre);

        txtDni.setText("Conductor con DNI "+ cargo.getDni());

        viewOrigen = false;
        viewDestino = false;

        lnlyTempOrder =null;

        lnLyPrecintos.setVisibility(View.GONE);
        lnLyCarreta.setVisibility(View.GONE);
        lnLyCarga.setVisibility(View.GONE);
        lnLyOrigen.setVisibility(View.GONE);
        lnLyDestino.setVisibility(View.GONE);
        lnLyNroDocumento.setVisibility(View.GONE);
        btnEscanear.setVisibility(View.GONE);
        lnLyPV.setVisibility(View.GONE);
        lnLyBultos.setVisibility(View.GONE);
        lnLyCargaVerificada.setVisibility(View.GONE);
        lnLyCodigoContenedor.setVisibility(View.GONE);
        lnLyTamanioContenedor.setVisibility(View.GONE);
        lnLyGuiaTicket.setVisibility(View.GONE);

        //Limpiar todos los below
        ((RelativeLayout.LayoutParams) lnLyCarreta.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyBultos.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyCarga.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyCargaVerificada.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyCodigoContenedor.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyDestino.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyOrigen.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyGuiaTicket.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyNroDocumento.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyPrecintos.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyPV.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) lnLyTamanioContenedor.getLayoutParams()).removeRule(RelativeLayout.BELOW);
        ((RelativeLayout.LayoutParams) btnEscanear.getLayoutParams()).removeRule(RelativeLayout.BELOW);


        int i = 0;
        //Formulario:
        for (CargoCargaFormDataDTO item: cargoCargaForm.Data) {
            //Log.e("codigo",item.Codigo);

            switch (item.Codigo){
                case "01":

                    txtOrigen.setText(item.Label);
                    spinOrigen.setPrompt(item.Label);

                    if(item.Visible){
                        viewOrigen = true;
                        //lnLyOrigen.setVisibility(View.VISIBLE);
                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyOrigen.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyOrigen;

                        }else{
                            resIdOrder = R.id.lnLyOrigen;
                        }
                    }
                    else{
                        viewOrigen = false;
                        //lnLyOrigen.setVisibility(View.GONE);
                    }
                    break;
                case "02":
                    txtDestino.setText(item.Label);
                    spinDestino.setPrompt(item.Label);

                    if(item.Visible){
                        viewDestino = true;

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyDestino.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyDestino;

                        }else{
                            resIdOrder = R.id.lnLyDestino;
                        }
                        //lnLyDestino.setVisibility(View.VISIBLE);
                    }
                    else{
                        viewDestino = false;
                        //lnLyDestino.setVisibility(View.GONE);
                    }

                    break;
                case "03"://NRO PRECINTOS:
                    etxtNroPrecintos.setHint(item.Label);
                    txtPrecintos.setText(item.Label);
                    if(item.Visible){

                        lnLyPrecintos.setVisibility(View.VISIBLE);
                        etxtNroPrecintos.setVisibility(View.VISIBLE);

                        if(reloadCarga){
                            etxtNroPrecintos.setText("");
                        }

                        if(limpiarDatosX){
                            etxtNroPrecintos.setText("");
                        }

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(item.CaracteresTamanio!=""){
                                if(Integer.valueOf(item.CaracteresTamanio)>0){
                                    filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                                }
                                else{
                                    filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                                }
                            }
                            else{
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }

                        }
                        else{
                            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        /*
                        if(item.CaracteresPermitidos != null){
                            //cadenaNroDocumento = item.CaracteresPermitidos;
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            etxtNroPrecintos.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                            //cadenaNroDocumento = cadenaAll;
                            InputFilter filter = createFilter(cadenaAll);
                            etxtNroPrecintos.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        */
                        //Fin Filtro

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyPrecintos.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyPrecintos;

                        }else{
                            resIdOrder = R.id.lnLyPrecintos;
                        }

                        if(etxtNroPrecintos.getText().toString() == null || etxtNroPrecintos.getText().toString().isEmpty())
                        {
                            if(cargo.getNumeroPrecintos() !=null && !cargo.getNumeroPrecintos().isEmpty()){
                                etxtNroPrecintos.setText(cargo.getNumeroPrecintos());
                            }
                        }
                        else if(etxtNroPrecintos.getText().toString() != null){
                            etxtNroPrecintos.setText(etxtNroPrecintos.getText().toString()+"");
                        }

                    }
                    else{
                        lnLyPrecintos.setVisibility(View.GONE);
                        etxtNroPrecintos.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("numeroPrecintos",null,1);
                    }

                    workPrecintos = true;
                    break;
                case "04"://Carreta

                    if(item.Visible){

                        etxtCarreta.setHint(item.Label);
                        txtCarreta.setText(item.Label);

                        etxtCarreta.setVisibility(View.VISIBLE);
                        lnLyCarreta.setVisibility(View.VISIBLE);

                        if(reloadCarga){
                            etxtCarreta.setText("");
                        }

                        if(limpiarDatosX){
                            etxtCarreta.setText("");
                        }

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(Integer.valueOf(item.CaracteresTamanio)>0){
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                            }
                            else{
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }
                        }
                        else{
                            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        if(item.CaracteresPermitidos != null){
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            etxtCarreta.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                            InputFilter filter = createFilter(cadenaAll);
                            etxtCarreta.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        //Fin Filtro

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyCarreta.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyCarreta;

                        }else{
                            resIdOrder = R.id.lnLyCarreta;
                        }

                        if(etxtCarreta.getText().toString() == null || etxtCarreta.getText().toString().isEmpty())
                        {
                            if(cargo.getCarreta() !=null && !cargo.getCarreta().isEmpty()){
                                etxtCarreta.setText(cargo.getCarreta());
                            }
                        }
                        else if(etxtCarreta.getText().toString() != null){
                            etxtCarreta.setText(etxtCarreta.getText().toString()+"");
                        }

                    }
                    else{
                        lnLyCarreta.setVisibility(View.GONE);
                        etxtCarreta.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("Carreta",null,1);
                    }


                    break;
                case "05"://Carga

                    if(item.Visible){
                        lnLyCarga.setVisibility(View.VISIBLE);
                        txtTieneCarga.setText(item.Label);
                        chBoxCarga.setVisibility(View.VISIBLE);

                        lnLyOrigen.setVisibility(View.GONE);
                        lnLyDestino.setVisibility(View.GONE);

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyCarga.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyCarga;

                        }else{
                            params = (RelativeLayout.LayoutParams) lnLyCarga.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            resIdOrder = R.id.lnLyCarga;
                        }

                        if(reloadCarga){
                            chBoxCarga.setChecked(false);
                        }

                        if(limpiarDatosX){
                            chBoxCarga.setChecked(false);
                        }

                        if(cargo.getIsCarga()!=null && !cargo.getIsCarga().isEmpty()){

                            chBoxCarga.setChecked(Boolean.valueOf(cargo.getIsCarga()));

                            if(cargo.getIsCarga().equalsIgnoreCase("true")){

                                if (cargo.getIsIngreso().equalsIgnoreCase("true")){
                                    if(viewOrigen){
                                        lnLyOrigen.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        lnLyOrigen.setVisibility(View.GONE);
                                    }
                                    lnLyDestino.setVisibility(View.GONE);
                                } else {
                                    if(viewDestino){
                                        lnLyDestino.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        lnLyDestino.setVisibility(View.GONE);
                                    }
                                    lnLyOrigen.setVisibility(View.GONE);
                                }
                            }
                            else{
                                lnLyOrigen.setVisibility(View.GONE);
                                lnLyDestino.setVisibility(View.GONE);
                            }
                        }
                    }
                    else{
                        lnLyCarga.setVisibility(View.GONE);
                        chBoxCarga.setVisibility(View.GONE);
                        lnLyOrigen.setVisibility(View.GONE);
                        lnLyDestino.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("isCarga",null,1);
                    }
                    break;
                case "06"://Nro Documento
                    cuarto_edt_or.setHint(item.Label);
                    txtNroDocumento.setText(item.Label);
                    //aplicaFiltroLenght = false;

                    if(reloadCarga){
                        cuarto_edt_or.setText("");
                    }

                    if(limpiarDatosX){
                        cuarto_edt_or.setText("");
                    }

                    if(limpiarDatosX){
                        cuarto_edt_or.setText("");
                    }

                    if(item.Visible){
                        cuarto_edt_or.setVisibility(View.VISIBLE);
                        lnLyNroDocumento.setVisibility(View.VISIBLE);

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(Integer.valueOf(item.CaracteresTamanio)>0){
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                            }
                            else{
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }
                        }
                        else{
                            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        if(item.CaracteresPermitidos != null){
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            cuarto_edt_or.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                            InputFilter filter = createFilter(cadenaAll);
                            cuarto_edt_or.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        //Fin Filtro

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyNroDocumento.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyNroDocumento;

                        }else{
                            resIdOrder = R.id.lnLyNroDocumento;
                        }

                        if(cuarto_edt_or.getText().toString() == null || cuarto_edt_or.getText().toString().isEmpty())
                        {
                            if(cargo.getNumeroDocumento() !=null && !cargo.getNumeroDocumento().isEmpty()){
                                cuarto_edt_or.setText(cargo.getNumeroDocumento());
                            }
                        }
                        else if(cuarto_edt_or.getText().toString() != null){
                            cuarto_edt_or.setText(cuarto_edt_or.getText().toString()+"");
                        }

                    }
                    else{
                        cuarto_edt_or.setVisibility(View.GONE);
                        lnLyNroDocumento.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("numeroDocumento",null,1);
                    }
                    break;
                case "07":
                    btnEscanear.setText(item.Label);

                    if(item.Visible){
                        btnEscanear.setVisibility(View.VISIBLE);

                        if(i>=1){
                            //Ordenar
                            params = (RelativeLayout.LayoutParams) btnEscanear.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.btnEscanear;

                        }else{
                            resIdOrder = R.id.btnEscanear;
                        }
                    }
                    else{
                        btnEscanear.setVisibility(View.GONE);
                    }
                    break;
                case "08"://PV
                    etxtPV.setHint(item.Label);
                    txtPV.setText(item.Label);


                    if(reloadCarga){
                        etxtPV.setText("");
                    }

                    if(limpiarDatosX){
                        etxtPV.setText("");
                    }

                    if(item.Visible){
                        etxtPV.setVisibility(View.VISIBLE);
                        lnLyPV.setVisibility(View.VISIBLE);

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(Integer.valueOf(item.CaracteresTamanio)>0){
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                            }
                            else{
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }
                        }
                        else{
                            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        if(item.CaracteresPermitidos != null){
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            etxtPV.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                            InputFilter filter = createFilter(cadenaAll);
                            etxtPV.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        //Fin Filtro

                        if(i>=1){
                            //Ordenar
                            params = (RelativeLayout.LayoutParams) lnLyPV.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyPV;

                        }else{
                            resIdOrder = R.id.lnLyPV;
                        }

                        if(etxtPV.getText().toString() == null || etxtPV.getText().toString().isEmpty())
                        {
                            if(cargo.getPv() !=null && !cargo.getPv().isEmpty()){
                                etxtPV.setText(cargo.getPv());
                            }
                        }
                        else if(etxtPV.getText().toString() != null){
                            etxtPV.setText(etxtPV.getText().toString()+"");
                        }

                    }
                    else{
                        etxtPV.setVisibility(View.GONE);
                        lnLyPV.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("pv",null,1);
                    }
                    break;
                case "09"://Cantidad de Bultos
                    etxtCantidadBultos.setHint(item.Label);
                    txtBultos.setText(item.Label);

                    if(reloadCarga){
                        etxtCantidadBultos.setText("");
                    }

                    if(limpiarDatosX){
                        etxtCantidadBultos.setText("");
                    }

                    if(item.Visible){
                        lnLyBultos.setVisibility(View.VISIBLE);
                        etxtCantidadBultos.setVisibility(View.VISIBLE);

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(item.CaracteresTamanio != ""){
                                if(Integer.valueOf(item.CaracteresTamanio)>0){
                                    filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                                }
                                else{
                                    //filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                                }
                            }else{
                                //filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }

                        }
                        else{
                            //filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        /*
                        if(item.CaracteresPermitidos != null){
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            etxtCantidadBultos.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                        */
                          //  InputFilter filter = createFilter(cadenaAll);
                          //  etxtCantidadBultos.setFilters(new InputFilter[] { filter,filterLenght });
                        //}
                        //Fin Filtro

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyBultos.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyBultos;

                        }else{
                            resIdOrder = R.id.lnLyBultos;
                        }

                        if(etxtCantidadBultos.getText().toString() == null || etxtCantidadBultos.getText().toString().isEmpty())
                        {
                            if(cargo.getCantidadBultos() !=null && !cargo.getCantidadBultos().isEmpty()){
                                etxtCantidadBultos.setText(cargo.getCantidadBultos());
                            }
                        }
                        else if(etxtCantidadBultos.getText().toString() != null){
                            etxtCantidadBultos.setText(etxtCantidadBultos.getText().toString()+"");
                        }

                    }
                    else{
                        lnLyBultos.setVisibility(View.GONE);
                        etxtCantidadBultos.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("CantidadBultos",null,1);
                    }
                    break;
                case "10"://Carga Verificada
                    txtCargaVerificada.setText(item.Label);

                    if(reloadCarga){
                        chBoxCargaVerificada.setChecked(false);
                    }

                    if(limpiarDatosX){
                        chBoxCargaVerificada.setChecked(false);
                    }

                    if(item.Visible){

                        lnLyCargaVerificada.setVisibility(View.VISIBLE);
                        chBoxCargaVerificada.setVisibility(View.VISIBLE);

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyCargaVerificada.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyCargaVerificada;

                        }else{
                            resIdOrder = R.id.lnLyCargaVerificada;
                        }

                        if(cargo.getIsCargaVerificada()!=null && !cargo.getIsCargaVerificada().isEmpty()){

                            chBoxCargaVerificada.setChecked(Boolean.valueOf(cargo.getIsCargaVerificada()));
                        }
                    }
                    else{
                        lnLyCargaVerificada.setVisibility(View.GONE);
                        chBoxCargaVerificada.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("isCargaVerificada",null,1);
                    }
                    break;

                case "11"://Codigo Contenedor
                    cuarto_edt_codContenedor.setHint(item.Label);
                    txtCodigoContenedor.setText(item.Label);

                    if(reloadCarga){
                        cuarto_edt_codContenedor.setText("");
                    }

                    if(limpiarDatosX){
                        cuarto_edt_codContenedor.setText("");
                    }

                    if(item.Visible){
                        lnLyCodigoContenedor.setVisibility(View.VISIBLE);
                        cuarto_edt_codContenedor.setVisibility(View.VISIBLE);

                        //Inicio Filtro
                        InputFilter filterLenght = null;
                        if(item.CaracteresTamanio != null){
                            if(Integer.valueOf(item.CaracteresTamanio)>0){
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(item.CaracteresTamanio));
                            }
                            else{
                                filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                            }
                        }
                        else{
                            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(500));
                        }

                        if(item.CaracteresPermitidos != null){
                            InputFilter filter = createFilter(item.CaracteresPermitidos);
                            cuarto_edt_codContenedor.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        else{
                            InputFilter filter = createFilter(cadenaAll);
                            cuarto_edt_codContenedor.setFilters(new InputFilter[] { filter,filterLenght });
                        }
                        //Fin Filtro

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyCodigoContenedor.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyCodigoContenedor;

                        }else{
                            resIdOrder = R.id.lnLyCodigoContenedor;
                        }

                        if(cuarto_edt_codContenedor.getText().toString() == null || cuarto_edt_codContenedor.getText().toString().isEmpty())
                        {
                            if(cargo.getCodigoContenedor() !=null && !cargo.getCodigoContenedor().isEmpty()){
                                cuarto_edt_codContenedor.setText(cargo.getCodigoContenedor());
                            }
                        }
                        else if(cuarto_edt_codContenedor.getText().toString() != null){
                            cuarto_edt_codContenedor.setText(cuarto_edt_codContenedor.getText().toString()+"");
                        }

                    }
                    else{
                        cuarto_edt_codContenedor.setVisibility(View.GONE);
                        lnLyCodigoContenedor.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("codigoContenedor",null,1);
                    }
                    break;
                case "12"://Tamaño Contenedor
                    txtTamanioContenedor.setText(item.Label);

                    if(reloadCarga){
                        switchTamanoContenedor.setChecked(false);
                    }

                    if(limpiarDatosX){
                        switchTamanoContenedor.setChecked(false);
                    }

                    if(item.Visible){
                        lnLyTamanioContenedor.setVisibility(View.VISIBLE);

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyTamanioContenedor.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyTamanioContenedor;

                        }else{
                            resIdOrder = R.id.lnLyTamanioContenedor;
                        }

                        if(cargo.getTamanoContenedor()!=null && !cargo.getTamanoContenedor().isEmpty()){

                            switchTamanoContenedor.setChecked(Boolean.valueOf(cargo.getTamanoContenedor()));
                        }
                    }
                    else{
                        lnLyTamanioContenedor.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("tamanoContenedor",null,1);
                    }
                    break;
                case "13"://Guia o Balance
                    //txtTamanioContenedor.setText(item.Label);
                    if(reloadCarga){
                        switchTipoGuiaBalance.setChecked(false);
                    }

                    if(limpiarDatosX){
                        switchTipoGuiaBalance.setChecked(false);
                    }

                    if(item.Visible){
                        lnLyGuiaTicket.setVisibility(View.VISIBLE);

                        if(i>=1){
                            params = (RelativeLayout.LayoutParams) lnLyGuiaTicket.getLayoutParams();
                            params.removeRule(RelativeLayout.BELOW);
                            params.addRule(RelativeLayout.BELOW,resIdOrder);
                            resIdOrder = R.id.lnLyGuiaTicket;

                        }else{
                            resIdOrder = R.id.lnLyGuiaTicket;
                        }

                        if(cargo.getGuiaRemision()!=null && !cargo.getGuiaRemision().isEmpty()){

                            switchTipoGuiaBalance.setChecked(Boolean.valueOf(cargo.getGuiaRemision()));
                        }
                    }
                    else{
                        lnLyGuiaTicket.setVisibility(View.GONE);
                        cargoCrud.updateFieldGeneric("GuiaRemision",null,1);
                    }
                    break;


            }

            i++;
        }

        if(!workPrecintos){
            cargoCrud.updateFieldGeneric("numeroPrecintos",null,1);
        }

        cargoCrud.updateFieldGeneric("UpdateTipoCarga","false",1);
    }

    public void drawPersonaForm(){

        Cargo cargo = cargoCrud.getCargo();


        //Placa
        if(cargo.getPlaca() != null && !cargo.getPlaca().isEmpty()){
            primero_edt_tracto.setText(cargo.getPlaca());

            if(cargo.getIsIngreso() != null && !cargo.getIsIngreso().isEmpty()){
                if (cargo.getIsIngreso().equalsIgnoreCase("true")){
                    primero_txt_mje.setText("El Tracto " + cargo.getPlaca()+" está Ingresando");
                } else {
                    primero_txt_mje.setText("El Tracto " + cargo.getPlaca()+" está Saliendo");
                }
            }

        }

        //Persona Foto Nombre Empresa DNI
        if(cargo.getDni()!=null && !cargo.getDni().isEmpty()){
            primero_edt_dni.setText(cargo.getDni());

            if(cargo.getJson()!=null){
                //ly_foto.setVisibility(View.VISIBLE);
                Gson gson = new Gson();
                JsonObject result = gson.fromJson(cargo.getJson(), JsonObject.class);
                cargo_txt_dni_persona.setText(result.get("persNombres").getAsString());
                cargo_txt_empresa_persona.setText(result.get("persEmpresa").getAsString());

                try {
                    Ion.with(img_cargo_persona)
                            .placeholder(R.drawable.ic_foto_fail)
                            .error(R.drawable.ic_foto_fail)
                            .load(result.get("Img").getAsString());

                } catch (Exception esvd){}
            }
        }


        //Casco
        if(cargo.getEppCasco()==null){

        }
        else{
            if(cargo.getEppCasco().equalsIgnoreCase("true"))
                check_casco.setChecked(true);
        }
        //Chaleco
        if(cargo.getEppChaleco()==null){

        }
        else{
            if(cargo.getEppChaleco().equalsIgnoreCase("true"))
                check_chaleco.setChecked(true);
        }
        //Botas
        if(cargo.getEppBotas()==null){

        }
        else{
            if(cargo.getEppBotas().equalsIgnoreCase("true"))
                check_botas.setChecked(true);
        }
        //Licencia Conducir
        if(cargo.getIsLicencia() != null){
            if(cargo.getIsLicencia().equalsIgnoreCase("false")){
                isLicencia.setChecked(true);
            }
        }

        //Autenticación


    }

    public void drawPersonaAlcolimetroForm(){

        lnlyAlcolimetro.setVisibility(View.GONE);
        Cargo cargo = cargoCrud.getCargo();

        //Alcolimetro
        Log.e("Ingreso Alcoholimetro","Alcoholimetro");

        if(cargoAlcolimetroDTO != null){
            Log.e("cargoAlcolimetroDTO x",cargoAlcolimetroDTO.toString());
            lnlyAlcolimetro.setVisibility(View.VISIBLE);
            chBoxPruebaAlcohol.setText(cargoAlcolimetroDTO.Titulo);
            txtPAPositivo.setText(cargoAlcolimetroDTO.Positivo);
            txtPANegativo.setText(cargoAlcolimetroDTO.Negativo);
        }
        else{
            //Ocultar:
            //lnlyAlcolimetro.setVisibility(View.GONE);
            Log.e("cargoAlcolimetroDTO x","Ocultar");
            lnlyAlcolimetro.setVisibility(View.GONE);
        }

        if(cargo.getAlcolimetro() != null){
            //Activar Check Box:
            chBoxPruebaAlcohol.setChecked(true);
            switchResultadoAlcoholimetro.setClickable(true);

            if(cargo.getAlcolimetro().equalsIgnoreCase("true")){
                switchResultadoAlcoholimetro.setChecked(false);
            }else{
                switchResultadoAlcoholimetro.setChecked(true);
            }
        }

    }

    //Registrar Placa
    public boolean ejecutarApiRegistro(String placaR, String GuidDipositivo){



        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(CargoFormActivity.this);
        pDialog.setMessage("Registrando Placa...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        String URL = urlApi.concat("api/Cargo/RegistrarPlaca");

        JsonObject json = new JsonObject();
        json.addProperty("DispositivoId", GuidDipositivo);
        json.addProperty("Placa", placaR);
        json.addProperty("CodigoEmpresa", "1");
        json.addProperty("CodigoTipoVehiculo", "6");

        Ion.with(this)
                .load("POST", URL)
                .setTimeout(1000*60)
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

    public boolean limpiarDatos(){

        try {

            cargoCrud.allCargoNull();

        } catch (Exception eew){
            Log.e("Exception limpiarDatos ", eew.getMessage());
        }

        //limpiarDatosRadioBoton();
        //getPersonaForm(urlApi);

        //adapterTipoCarga = null;
        adapterTipoCarga.removeSelection();
        cargoFotoFormDTO = null;
        gvTipoFotoResized = false;
        reloadCarga = true;

        DBHelper dbgelperDeete = new DBHelper(this);
        SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM CargoPrecinto");
        sqldbDelete.close();

        //vpCargoForm.setCurrentItem(1);

        return true;
    }

    public boolean limpiarDatosRadioBoton(){

        try {
            DBHelper dbHelperNumero = new DBHelper(mContext);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            //dbNro.execSQL("UPDATE Cargo SET TipoCarga = '1' WHERE CargoId = 1");
            dbNro.execSQL("UPDATE Cargo SET EppCasco = 'false'");
            dbNro.execSQL("UPDATE Cargo SET EppChaleco = 'false'");
            dbNro.execSQL("UPDATE Cargo SET EppBotas = 'false'");
            dbNro.execSQL("UPDATE Cargo SET isCarga = 'false'");
            dbNro.execSQL("UPDATE Cargo SET isLicencia = 'true'");
            dbNro.close();
        } catch (Exception eew){}


        return true;
    }

    public void MensajeError(String title, String mensaje){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);
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

    public void MensajePersona(String mensaje) {

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);
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

    //Navegar por wizzard cargo
    public void ViewCargaForm(View view){

        //Log.e("Nombre",tipoCargaSelected.Nombre);
        //Log.e("Id",String.valueOf(tipoCargaSelected.ClienteCargaId));
        //Log.e("Item",String.valueOf(vpCargoForm.getCurrentItem()));
        vpCargoForm.setCurrentItem(1);
    }

    public void ViewCargaGoForm(View view){

        CargoTipoCargaDTO tipoCargaSelected = adapterTipoCarga.getItemX();

        //Validar placa:
        Cargo cargo = cargoCrud.getCargo();

        if(cargo.getPlaca() == null || cargo.getPlaca().isEmpty()){
            Log.e("Placa Go","Placa vacia");
            Toast.makeText(mContext, "No cuenta con placa", Toast.LENGTH_SHORT).show();
            return;
        }

        //Tipo de Carga:
        if(cargo.getTipoCarga() == null || cargo.getTipoCarga().isEmpty()){
            Toast.makeText(mContext, "Seleccione Tipo de Carga", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            if(adapterTipoCarga==null){
                Toast.makeText(mContext, "Seleccione Tipo de Carga null", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                if(adapterTipoCarga.getItemX().ClienteCargaId == ""){
                    Toast.makeText(mContext, "Seleccione Tipo de Carga s/c", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        //Se ejecutó DNI
        if(cargo.getDni() == null || cargo.getDni().isEmpty()){
            Toast.makeText(mContext, "Validar Documento de Identidad", Toast.LENGTH_SHORT).show();
            return;
        }

        vpCargoForm.setCurrentItem(1);


    }

    //Navegar por wizzard cargo
    public void ViewPersonaForm(View view){

        /*
        CargoTipoCargaDTO tipoCargaSelected = adapterTipoCarga.getItemX();

        if(tipoCargaSelected.ClienteCargaId == ""){

            Toast.makeText(mContext, "Seleccione Tipo de Carga", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.e("Nombre",tipoCargaSelected.Nombre);
        Log.e("Id",String.valueOf(tipoCargaSelected.ClienteCargaId));
        */

        //Log.e("Item",String.valueOf(vpCargoForm.getCurrentItem()));
        vpCargoForm.setCurrentItem(0);
    }

    public void ViewFotoForm(View view){
        boolean valida = validaFormCarga();
        if(valida){
            vpCargoForm.setCurrentItem(2);
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox)view).isChecked();

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
                break;

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

            case R.id.chBoxPruebaAlcohol:


                //Log.e("Check Box", "ALcolimetro" );

                if(checked){

                    //Habilitar el swith
                    Boolean _alcolimetro =  switchResultadoAlcoholimetro.isChecked();
                    switchResultadoAlcoholimetro.setClickable(true);

                    if(_alcolimetro){

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET Alcolimetro = 'false'");
                            dba.close();
                        } catch (Exception eew){}

                    }
                    else{

                        try {
                            DBHelper dbHelperAlarm = new DBHelper(mContext);
                            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                            dba.execSQL("UPDATE Cargo SET Alcolimetro = 'true'");
                            dba.close();
                        } catch (Exception eew){}

                    }
                }else{
                    switchResultadoAlcoholimetro.setClickable(false);
                    try {
                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Cargo SET Alcolimetro = "+null);
                        dba.close();
                    } catch (Exception eew){}

                }
        }
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

    public void visualizarImagen(String uri,int tipoFoto){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);
        mView = getLayoutInflater().inflate(R.layout.popup_visualizacion, null);
        mBuilder.setCancelable(false);

        ImageView img = (ImageView) mView.findViewById(R.id.popup_img_visualizacion);
        Uri myUri = Uri.parse(uri);
        img.setImageURI(myUri);

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            });
            mBuilder.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (tipoFoto == 1){//1 Precinto

                        try {
                            DBHelper dataBaseHelperB = new DBHelper(mContext);
                            SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                            dbU.execSQL("UPDATE CargoPrecinto SET Foto = " + null+" WHERE Foto = '"+uri+"'");
                            dbU.close();

                        } catch (Exception e){}

                        loadPrecinto();

                    }
                    else if(tipoFoto == 2){

                        //Log.e()

                        try {
                            DBHelper dataBaseHelperB = new DBHelper(mContext);
                            SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                            dbU.execSQL("UPDATE CargoTipoFoto SET FilePath = " + null+" WHERE FilePath = '"+uri+"'");
                            //dbNro.execSQL("UPDATE CargoTipoFoto SET FilePath = '"+filePath+"' WHERE ClienteCargaFotoId = '"+objFotoWork.getClienteCargaFotoId()+"'");
                            dbU.close();

                        } catch (Exception e){}

                        loadCargoTipoFoto();

                    }

                    dialog.dismiss();

                    /*else {

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
                    }*/




                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fotoTipoCarga(CargoFotoDataModel CargoFotoDataModelX){

        //tipoFoto = 4;
        //numeroPrecintoFoto = numeroPrecinto;
        cargoFotoDataModel = CargoFotoDataModelX;
        callCameraAsync();

    }

    public void fotoPrecinto(String numeroPrecinto){

        //tipoFoto = 4;
        numeroPrecintoFoto = numeroPrecinto;
        callCameraAsync();

    }

    public void callCameraAsync(){
        //Log.e(TAG,"Call Camera");
        /*
        try{
            File filecc = createImageFile();//new File(path);
            Uri outputFileUri = Uri.fromFile( filecc );
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
            intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
            startActivityForResult( intent, REQUEST_CODE_PHOTO_TAKEN_ASYNC );
        }
        catch (Exception e){
            //Log.e(TAG,e.toString());
        }
        */
        /*

startActivityForResult(pictureIntent, REQUEST_CODE_PHOTO_TAKEN_ASYNC);

        */

        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch (IOException ex){

            }
            if(photoFile != null){
                String appId = mContext.getPackageName();
                appId = appId+".provider";
                Log.e("auth",appId);
                Uri photoURI = FileProvider.getUriForFile(mContext,appId, photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent,REQUEST_CODE_PHOTO_TAKEN_ASYNC);
            }
        }

    }

    private File createImageFile() throws IOException {

        //Log.e(TAG,"Create file");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory()+"/SOLGIS/Cargo");//getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        imageFilePath = image.getAbsolutePath();
        //imageReducedFilePath = image.getAbsolutePath();

        //Log.e(TAG,"File Paths");
        //Log.e(TAG,imageFilePath);
        //Log.e(TAG,imageReducedFilePath);

        return image;
    }

    public void mensajeTimeOut(){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);
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

    public void ViewPersonaFormX(View view){

        limpiarDatos();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("Conforme", new DialogInterface.OnClickListener() {
            //
            //review
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cargoFotoFormDTO = null;
                cargoCargaForm = null;
                //gridViewResized=false;
                vpCargoForm.setCurrentItem(0);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("Conforme", new DialogInterface.OnClickListener() {
            //
            //review
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cargoFotoFormDTO = null;
                cargoCargaForm = null;
                //gridViewResized=false;
                vpCargoForm.setCurrentItem(0);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void resizeGridView(GridView gridView, int items, int columns) {
        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        int rows = 0;

        if(heightTipoCarga==0){

            rows = (int)(items / columns);

            int remainder = items % columns;

            if(remainder>0){

                rows++;
            }
            heightTipoCarga = convertDpToPixels(40,mContext);

        }
        else{
            rows = (int)(items / columns);
            int remainder = items % columns;

            if(remainder>0){

                rows++;
            }

        }

        Log.e("rows: ",String.valueOf(rows));
        Log.e("oneRowHeight",String.valueOf(heightTipoCarga));
        //Log.e("",String.valueOf());
        //params.height = oneRowHeight * rows;
        params.height = heightTipoCarga*rows;
        gridView.setLayoutParams(params);
    }

    private void resizeGridViewFotos(GridView gridView, int items) {

        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        int columns = gridView.getNumColumns();
        int rows = 0;

        columnsCargaFoto = columns;


        if(heightTipoFoto==0){

            rows = (int)(items / columns);
            int remainder = items % columns;

            if(remainder>0){
                rows++;
            }

            heightTipoFoto = convertDpToPixels(210,mContext);
        }

        params.height = heightTipoFoto*rows;
        gridView.setLayoutParams(params);
    }

    private void resizeGridViewFotosPrecintos(GridView gridView, int items) {

        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        //Se obtiene el alto total
        int columns = gridView.getNumColumns();
        int rows = 0;

        columnsFotoPrecinto = columns;

        if(heightFotoPrecinto==0){

            rows = (int)(items / columns);
            int remainder = items % columns;

            if(remainder>0){
                rows++;
            }

            heightFotoPrecinto = convertDpToPixels(210,mContext);
        }

        params.height = (heightFotoPrecinto*rows);
        gridView.setLayoutParams(params);
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
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

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CargoFormActivity.this);
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


}
