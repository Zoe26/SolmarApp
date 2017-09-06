package com.idslatam.solmar.People;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.ImageClass.ImageConverter;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.bitmap.Transform;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class PeopleDetalle extends AppCompatActivity {

    Context mContext;

    LinearLayout people_detalle_mensaje;
    TextView people_txt_mensaje, people_detalle_txtfoto_valor, people_detalle_txtfoto_vehiculo;
    EditText people_detalle_dni, people_edt_persTipo, people_detalle_nombre, people_detalle_empresa,
            people_detalle_motivo, people_detalle_codArea;

    String GuidDipositivo;

    private static final int CAPTURE_MEDIA = 368;
    private Activity activity;

    String Uri_Foto, URL_API, fotoVal, fotoVeh;

    boolean fotoValor = true, fotoVehiculo = true;

    Button people_detalle_btn_registrar;

    ImageView people_detalle_img;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_detalle);

        mContext = this;
        activity = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        people_detalle_mensaje = (LinearLayout)findViewById(R.id.people_detalle_mensaje);
        people_txt_mensaje = (TextView)findViewById(R.id.people_txt_mensaje);

        people_edt_persTipo = (EditText)findViewById(R.id.people_edt_persTipo);
        people_detalle_dni = (EditText) findViewById(R.id.people_detalle_dni);
        people_detalle_nombre = (EditText) findViewById(R.id.people_detalle_nombre);
        people_detalle_empresa = (EditText) findViewById(R.id.people_detalle_empresa);
        people_detalle_codArea = (EditText) findViewById(R.id.people_detalle_codArea);
        people_detalle_motivo = (EditText) findViewById(R.id.people_detalle_motivo);

        people_detalle_txtfoto_valor = (TextView)findViewById(R.id.people_detalle_txtfoto_valor);
        people_detalle_txtfoto_vehiculo = (TextView)findViewById(R.id.people_detalle_txtfoto_vehiculo);

        people_detalle_img = (ImageView)findViewById(R.id.people_detalle_img);

        people_detalle_btn_registrar = (Button)findViewById(R.id.people_detalle_btn_registrar);

        Log.e(" PEOPLE DETALLE ", "onCreate");
        loadDatosPeople();

    }

    public void loadDatosPeople(){

        Log.e(" PEOPLE DETALLE ", "loadDatosPeople");

        String json = null, dni = null, fotoValor = null, fotoVehiculo = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoValor = c.getString(c.getColumnIndex("fotoValor"));
                fotoVehiculo = c.getString(c.getColumnIndex("fotoVehiculo"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Log.e(" PEOPLE dni ", dni);
        Log.e(" PEOPLE json ", json);

        if (json == null){return;}

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Log.e(" PEOPLE DETALLE ", json.toString());

        if (!jsonObject.get("Resultado").isJsonNull()){

            if (jsonObject.get("Resultado").getAsString().equalsIgnoreCase("OK")){
                people_detalle_mensaje.setBackgroundColor(Color.parseColor("#00796B"));
            } else {
                people_detalle_mensaje.setBackgroundColor(Color.parseColor("#FF5252"));
                people_detalle_btn_registrar.setEnabled(false);
            }

        } else {

            people_detalle_mensaje.setBackgroundColor(Color.parseColor("#FF5252"));
            people_detalle_btn_registrar.setEnabled(false);

        }


        try {

            ImageView imageViewb = (ImageView)findViewById(R.id.people_detalle_img);

            try {

                Ion.with(imageViewb)
                        .placeholder(R.drawable.ic_foto_fail)
                        .error(R.drawable.ic_foto_fail)
                        .transform(new Transform() {
                            @Override
                            public Bitmap transform(Bitmap b) {
                                return ImageConverter.createCircleBitmap(b);
                            }

                            @Override
                            public String key() {
                                Log.e("key "," null");
                                return null;
                            }
                        })
                        .load(jsonObject.get("Img").getAsString());

            } catch (Exception e){}

        } catch (Exception es){}


        people_txt_mensaje.setText(jsonObject.get("Header").getAsString());

        if (jsonObject.get("persNombres").isJsonNull()){
            return;
        }

        if (!jsonObject.get("persTipo").isJsonNull()){
            people_edt_persTipo.setText(jsonObject.get("persTipo").getAsString());
        }

        people_detalle_nombre.setText(jsonObject.get("persNombres").getAsString());
        people_detalle_empresa.setText(jsonObject.get("persEmpresa").getAsString());
        people_detalle_motivo.setText(jsonObject.get("persMotivo").getAsString());
        //.setText(dni);

        if (!jsonObject.get("NroDOI").isJsonNull()){
            people_detalle_dni.setText(jsonObject.get("NroDOI").getAsString());
        } else {
            people_detalle_dni.setText("");
        }

        people_detalle_codArea.setText(jsonObject.get("persArea").getAsString());

        if (fotoValor!=null){
            people_detalle_txtfoto_valor.setText("¡Foto valor guardada!");
        }

        if (fotoVehiculo!=null){
            people_detalle_txtfoto_vehiculo.setText("¡Foto vehículo guardada!");
        }

    }

    public void salir(View view){
        mensajeSalir();
    }

    public void fotoValor(View view){
        fotoValor = true;
        fotoVehiculo = false;
        tomarFoto();
    }

    public void fotoVehiculo(View view){
        fotoValor = false;
        fotoVehiculo = true;
        tomarFoto();
    }

    public void tomarFoto(){

        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_MEDIUM)
                .enableImageCropping(false)
                .launchCamera();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {
            Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));

            String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            Uri_Foto = photoUri;

            if (fotoValor){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(this);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE People SET fotoValor = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoValor ","true");
                } catch (Exception eew){}


                people_detalle_txtfoto_valor.setText("¡Foto valor guardada!");

                //imgEstadoDelantera.setImageResource(R.drawable.ic_check_foto);
                fotoValor = false;

            } else if (fotoVehiculo){

                try {
                    DBHelper dbHelperAlarm = new DBHelper(this);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE People SET fotoVehiculo = '"+Uri_Foto+"'");
                    dba.close();
                    Log.e("fotoVehiculo ","true");
                } catch (Exception eew){}

                people_detalle_txtfoto_vehiculo.setText("¡Foto vehículo guardada!");

                //imgEstadoPaniramica.setImageResource(R.drawable.ic_check_foto);
                fotoVehiculo = false;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void guardarPeople(View view){
        guardarPeopleM();
    }

    public void guardarPeopleM(){


        pDialog = new ProgressDialog(PeopleDetalle.this);
        pDialog.setMessage("Registrando...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

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


        String dni = null, json = null;
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoVal = c.getString(c.getColumnIndex("fotoValor"));
                fotoVeh = c.getString(c.getColumnIndex("fotoVehiculo"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (fotoVal==null && fotoVeh==null){casoSinNada(jsonObject);
        } else  if (fotoVal!=null && fotoVeh==null){casoConMaterial(jsonObject);
        } else  if (fotoVal==null && fotoVeh!=null){casoConVehiculo(jsonObject);
        } else  if (fotoVal!=null && fotoVeh!=null){casoConMaterialVehiculo(jsonObject);}

        Log.e("JsonObject SEND ", jsonObject.toString());

    }

    public void casoConMaterial(JsonObject result){

        String URL = URL_API.concat("api/People/Create");

        Ion.with(this)
                .load(URL)
                .setMultipartParameter("DispositivoId", GuidDipositivo)
                .setMultipartParameter("codPers", result.get("codPers").getAsString())
                .setMultipartParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setMultipartParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setMultipartParameter("codMotivo", result.get("codMotivo").getAsString())
                .setMultipartParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setMultipartParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setMultipartParameter("codArea", result.get("codArea").getAsString())
                .setMultipartParameter("nroPase", result.get("nroPase").getAsString())
                .setMultipartParameter("persTipo", result.get("persTipo").getAsString())
                .setMultipartFile("Material", new File(fotoVal))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            Log.e("Exception ", e.getMessage());
                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            return;

                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("People Create ", result.toString());
                            //Estado

                            if (result.get("Estado").getAsString().equalsIgnoreCase("false")){
                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception edsv){}

                                showDialogError();

                                return;
                            } else {

                                limpiarDatos();

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
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });

    }

    public void casoConVehiculo(JsonObject result){

        String URL = URL_API.concat("api/People/Create");

        Ion.with(this)
                .load(URL)
                .setMultipartParameter("DispositivoId", GuidDipositivo)
                .setMultipartParameter("codPers", result.get("codPers").getAsString())
                .setMultipartParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setMultipartParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setMultipartParameter("codMotivo", result.get("codMotivo").getAsString())
                .setMultipartParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setMultipartParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setMultipartParameter("codArea", result.get("codArea").getAsString())
                .setMultipartParameter("nroPase", result.get("nroPase").getAsString())
                .setMultipartParameter("persTipo", result.get("persTipo").getAsString())
                .setMultipartFile("Vehiculo", new File(fotoVeh))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            Log.e("Exception ", e.getMessage());
                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            return;

                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("People Create ", result.toString());
                            //Estado

                            if (result.get("Estado").getAsString().equalsIgnoreCase("false")){
                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception edsv){}

                                showDialogError();

                                return;
                            } else {

                                limpiarDatos();

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
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });

    }

    public void casoConMaterialVehiculo(JsonObject result){

        String URL = URL_API.concat("api/People/Create");

        Ion.with(this)
                .load(URL)
                .setMultipartParameter("DispositivoId", GuidDipositivo)
                .setMultipartParameter("codPers", result.get("codPers").getAsString())
                .setMultipartParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setMultipartParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setMultipartParameter("codMotivo", result.get("codMotivo").getAsString())
                .setMultipartParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setMultipartParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setMultipartParameter("codArea", result.get("codArea").getAsString())
                .setMultipartParameter("nroPase", result.get("nroPase").getAsString())
                .setMultipartParameter("persTipo", result.get("persTipo").getAsString())
                .setMultipartFile("Vehiculo", new File(fotoVeh))
                .setMultipartFile("Material", new File(fotoVal))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            Log.e("Exception ", e.getMessage());
                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            return;

                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("People Create ", result.toString());
                            //Estado

                            if (result.get("Estado").getAsString().equalsIgnoreCase("false")){
                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception edsv){}

                                showDialogError();

                                return;
                            } else {

                                limpiarDatos();

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
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });

    }

    public void casoSinNada(JsonObject result){

        String URL = URL_API.concat("api/People/Create");

        Ion.with(this)
                .load(URL)
                .setMultipartParameter("DispositivoId", GuidDipositivo)
                .setMultipartParameter("codPers", result.get("codPers").getAsString())
                .setMultipartParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setMultipartParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setMultipartParameter("codMotivo", result.get("codMotivo").getAsString())
                .setMultipartParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setMultipartParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setMultipartParameter("codArea", result.get("codArea").getAsString())
                .setMultipartParameter("nroPase", result.get("nroPase").getAsString())
                .setMultipartParameter("persTipo", result.get("persTipo").getAsString())
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            Log.e("Exception ", e.getMessage());
                            Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            return;

                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("People Create ", result.toString());
                            //Estado

                            if (result.get("Estado").getAsString().equalsIgnoreCase("false")){
                                try {
                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                } catch (Exception edsv){}

                                showDialogError();

                                return;
                            } else {

                                limpiarDatos();

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
                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });

    }

    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                limpiarDatos();
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
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
                dialog.dismiss();
                limpiarDatos();
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

    public void showDialogError(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("No se pudo guardar el registro. Intente nuevamente por favor.");

        builder.setPositiveButton("Reenviar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                guardarPeopleM();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public boolean limpiarDatos(){

        try {

            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();

            dba.close();

        } catch (Exception edc){}

        try {

            DBHelper dbHelperAlarm = new DBHelper(this);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE People SET fotoVehiculo = " + null);
            dba.execSQL("UPDATE People SET fotoValor = " + null);
            dba.execSQL("UPDATE People SET dni = "+null);
            dba.close();

        } catch (Exception eew){}

        return true;
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

}
