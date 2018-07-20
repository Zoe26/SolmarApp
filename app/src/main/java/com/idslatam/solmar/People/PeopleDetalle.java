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
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.ImageClass.ImageConverter;
import com.idslatam.solmar.Models.Crud.PeopleFotoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.*;
import com.idslatam.solmar.Models.Entities.DTO.Patrol.PatrolTakeFotoAsync;
import com.idslatam.solmar.Models.Entities.DTO.People.PeopleTakeFotoAsync;
import com.idslatam.solmar.Patrol.PatrolActivity;
import com.idslatam.solmar.R;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.security.AccessController.getContext;

public class PeopleDetalle extends AppCompatActivity implements View.OnClickListener {

    Context mContext;

    LinearLayout people_detalle_mensaje, people_detalle_datos, people_detalle_cuarto,
            people_detalle_quinto, people_detalle_aux, people_detalle_sexto, people_detalle_septimo;

    TextView people_txt_mensaje, people_detalle_mensaje_error;

    EditText people_detalle_dni, people_edt_persTipo, people_detalle_nombre, people_detalle_empresa,
            people_detalle_motivo, people_detalle_codArea;

    String GuidDipositivo,imageFilePath,CodigoSincronizacion=null,Numero=null,DispositivoId;

    private static final int CAPTURE_MEDIA = 368;
    private Activity activity;
    private static final String TAG = "PeopleAsync";
    private static final int REQUEST_CODE_PHOTO_TAKEN_ASYNC = 2;

    String Uri_Foto, URL_API, fotoVal, fotoVeh, fotoVehGuantera, fotoVehMaletera;

    boolean fotoValor = true, fotoVehiculo = true, fotoVehiculoGuantera = true, fotoVehiculoMaletera = true;

    Button people_detalle_btn_registrar;

    ImageButton btn_visualizar_valor, btn_visualizar_vehiculo_delatera, btn_visualizar_vehiculo_guantera
            , btn_visualizar_vehiculo_maletera;

    ImageView people_detalle_img;

    int indice,TIME_OUT = 5*60 * 1000;

    ProgressDialog pDialog;

    List<PeopleFoto> peopleFotoList = new ArrayList<PeopleFoto>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_detalle);

        final File newFile = new File(Environment.getExternalStorageDirectory() + "/Solgis/People");
        newFile.mkdirs();

        mContext = this;
        activity = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        people_detalle_mensaje = (LinearLayout)findViewById(R.id.people_detalle_mensaje);
        people_detalle_datos = (LinearLayout)findViewById(R.id.people_detalle_datos);
        people_detalle_cuarto = (LinearLayout)findViewById(R.id.people_detalle_cuarto);
        people_detalle_quinto = (LinearLayout)findViewById(R.id.people_detalle_quinto);

        people_detalle_sexto = (LinearLayout)findViewById(R.id.people_detalle_sexto);
        people_detalle_septimo = (LinearLayout)findViewById(R.id.people_detalle_septimo);

        people_detalle_aux = (LinearLayout)findViewById(R.id.people_detalle_aux);

        btn_visualizar_valor = (ImageButton)findViewById(R.id.btn_visualizar_valor);
        btn_visualizar_vehiculo_delatera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_delatera);
        btn_visualizar_vehiculo_guantera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_guantera);
        btn_visualizar_vehiculo_maletera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_maletera);

        btn_visualizar_valor.setOnClickListener(this);
        btn_visualizar_vehiculo_delatera.setOnClickListener(this);
        btn_visualizar_vehiculo_guantera.setOnClickListener(this);
        btn_visualizar_vehiculo_maletera.setOnClickListener(this);

        people_txt_mensaje = (TextView)findViewById(R.id.people_txt_mensaje);
        people_detalle_mensaje_error = (TextView)findViewById(R.id.people_detalle_mensaje_error);

        people_edt_persTipo = (EditText)findViewById(R.id.people_edt_persTipo);
        people_detalle_dni = (EditText) findViewById(R.id.people_detalle_dni);
        people_detalle_nombre = (EditText) findViewById(R.id.people_detalle_nombre);
        people_detalle_empresa = (EditText) findViewById(R.id.people_detalle_empresa);
        people_detalle_codArea = (EditText) findViewById(R.id.people_detalle_codArea);
        people_detalle_motivo = (EditText) findViewById(R.id.people_detalle_motivo);

        people_detalle_img = (ImageView)findViewById(R.id.people_detalle_img);

        people_detalle_btn_registrar = (Button)findViewById(R.id.people_detalle_btn_registrar);

        Log.e(" PEOPLE DETALLE ", "onCreate");
        loadDatosPeople();

    }


    @Override
    public void onClick(View v) {

        String fotoValorA = null, fotoVehiculoA = null
                , fotoVehiculoGuanteraA = null, fotoVehiculoMaleteraA = null;

        switch (v.getId()) {
            case R.id.btn_visualizar_valor:

                try {
                    DBHelper dataBaseHelper = new DBHelper(this);
                    SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                    String selectQuery = "SELECT fotoValor FROM People";
                    Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                    if (c.moveToFirst()) {
                        fotoValorA = c.getString(c.getColumnIndex("fotoValor"));

                    }
                    c.close();
                    dbst.close();

                } catch (Exception e) {}

                indice = 1;

                visualizarImagen(fotoValorA);

                break;

            case R.id.btn_visualizar_vehiculo_delatera:

                try {
                    DBHelper dataBaseHelper = new DBHelper(this);
                    SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                    String selectQuery = "SELECT fotoVehiculo FROM People";
                    Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                    if (c.moveToFirst()) {
                        fotoVehiculoA = c.getString(c.getColumnIndex("fotoVehiculo"));

                    }
                    c.close();
                    dbst.close();

                } catch (Exception e) {}

                indice = 2;

                visualizarImagen(fotoVehiculoA);

                break;

            case R.id.btn_visualizar_vehiculo_guantera:

                try {
                    DBHelper dataBaseHelper = new DBHelper(this);
                    SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                    String selectQuery = "SELECT fotoVehiculoGuantera FROM People";
                    Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                    if (c.moveToFirst()) {
                        fotoVehiculoGuanteraA = c.getString(c.getColumnIndex("fotoVehiculoGuantera"));
                    }
                    c.close();
                    dbst.close();

                } catch (Exception e) {}

                indice = 3;

                visualizarImagen(fotoVehiculoGuanteraA);

                break;

            case R.id.btn_visualizar_vehiculo_maletera:

                try {
                    DBHelper dataBaseHelper = new DBHelper(this);
                    SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                    String selectQuery = "SELECT fotoVehiculoMaletera FROM People";
                    Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                    if (c.moveToFirst()) {
                        fotoVehiculoMaleteraA = c.getString(c.getColumnIndex("fotoVehiculoMaletera"));
                    }
                    c.close();
                    dbst.close();

                } catch (Exception e) {}

                indice = 4;

                visualizarImagen(fotoVehiculoMaleteraA);

                break;
        }

    }


    public void loadDatosPeople(){

        btn_visualizar_valor.setVisibility(View.GONE);
        btn_visualizar_vehiculo_delatera.setVisibility(View.GONE);
        btn_visualizar_vehiculo_guantera.setVisibility(View.GONE);
        btn_visualizar_vehiculo_maletera.setVisibility(View.GONE);

        Log.e(" PEOPLE DETALLE ", "loadDatosPeople");

        String json = null, dni = null, fotoValor = null, fotoVehiculoA = null
                , fotoVehiculoGuanteraA = null, fotoVehiculoMaleteraA = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo, fotoVehiculoGuantera, fotoVehiculoMaletera FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoValor = c.getString(c.getColumnIndex("fotoValor"));
                fotoVehiculoA = c.getString(c.getColumnIndex("fotoVehiculo"));
                fotoVehiculoGuanteraA = c.getString(c.getColumnIndex("fotoVehiculoGuantera"));
                fotoVehiculoMaleteraA = c.getString(c.getColumnIndex("fotoVehiculoMaletera"));

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
                people_detalle_aux.setVisibility(View.VISIBLE);

                if (!jsonObject.get("Mensaje").isJsonNull()){
                    people_detalle_mensaje_error.setText(jsonObject.get("Mensaje").getAsString());
                }

                //people_detalle_datos.setVisibility(View.GONE);
                //people_detalle_img.setVisibility(View.GONE);

                people_detalle_btn_registrar.setVisibility(View.GONE);
                people_detalle_cuarto.setVisibility(View.GONE);
                people_detalle_quinto.setVisibility(View.GONE);
                people_detalle_sexto.setVisibility(View.GONE);
                people_detalle_septimo.setVisibility(View.GONE);

            }

        } else {

            people_detalle_mensaje.setBackgroundColor(Color.parseColor("#FF5252"));
            people_detalle_btn_registrar.setEnabled(false);

        }

        String foto = null;

        if (!jsonObject.get("Img").isJsonNull()){foto = jsonObject.get("Img").getAsString();}


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
                        .load(foto);

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
            btn_visualizar_valor.setEnabled(true);
            btn_visualizar_valor.setVisibility(View.VISIBLE);
            btn_visualizar_valor.setImageURI(Uri.parse(fotoValor));
        }

        if (fotoVehiculoA!=null){
            btn_visualizar_vehiculo_delatera.setEnabled(true);
            btn_visualizar_vehiculo_delatera.setVisibility(View.VISIBLE);
            btn_visualizar_vehiculo_delatera.setImageURI(Uri.parse(fotoVehiculoA));
        }

        if (fotoVehiculoGuanteraA!=null){
            btn_visualizar_vehiculo_guantera.setEnabled(true);
            btn_visualizar_vehiculo_guantera.setVisibility(View.VISIBLE);
            btn_visualizar_vehiculo_guantera.setImageURI(Uri.parse(fotoVehiculoGuanteraA));
        }

        if (fotoVehiculoMaleteraA!=null){
            btn_visualizar_vehiculo_maletera.setEnabled(true);
            btn_visualizar_vehiculo_maletera.setVisibility(View.VISIBLE);
            btn_visualizar_vehiculo_maletera.setImageURI(Uri.parse(fotoVehiculoMaleteraA));
        }

    }

    public void salir(View view){
        mensajeSalir();
    }

    public void fotoValor(View view){
        fotoValor = true;
        fotoVehiculo = false;
        fotoVehiculoGuantera = false;
        fotoVehiculoMaletera = false;
        tomarFoto();
    }

    public void fotoVehiculo(View view){
        fotoValor = false;
        fotoVehiculo = true;
        fotoVehiculoGuantera = false;
        fotoVehiculoMaletera = false;
        tomarFoto();
    }

    public void fotoVehiculoGuantera(View view){
        fotoValor = false;
        fotoVehiculo = false;
        fotoVehiculoGuantera = true;
        fotoVehiculoMaletera = false;
        tomarFoto();
    }

    public void fotoVehiculoMaletera(View view){
        fotoValor = false;
        fotoVehiculo = false;
        fotoVehiculoGuantera = false;
        fotoVehiculoMaletera = true;
        tomarFoto();
    }

    public void tomarFoto(){

        /*
        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_LOWEST)
                .enableImageCropping(false)
                .launchCamera();
        */
        try{
            File filecc = createImageFile();
            Uri outputFileUri = Uri.fromFile( filecc );
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
            intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
            startActivityForResult( intent, REQUEST_CODE_PHOTO_TAKEN_ASYNC );
        }
        catch (Exception e){
            Log.e(TAG,e.toString());
        }

    }

    private File createImageFile() throws IOException {

        Log.e("People","Create file");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory()+"/Solgis/People");//getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        imageFilePath = image.getAbsolutePath();
        //imageReducedFilePath = image.getAbsolutePath();

        Log.e(TAG,"File Paths");
        Log.e(TAG,imageFilePath);
        //Log.e(TAG,imageReducedFilePath);

        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_CODE_PHOTO_TAKEN_ASYNC && resultCode == RESULT_OK) {

            Log.e("File", "" + imageFilePath);

            //String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            Uri_Foto = imageFilePath;

            if (fotoValor){

                PeopleTakeFotoAsync fotoPeople =  new PeopleTakeFotoAsync(imageFilePath,"1");
                new takePhotoAsync().execute(fotoPeople);

            } else if (fotoVehiculo){

                PeopleTakeFotoAsync fotoPeople =  new PeopleTakeFotoAsync(imageFilePath,"2");
                new takePhotoAsync().execute(fotoPeople);

            } else if (fotoVehiculoGuantera){

                PeopleTakeFotoAsync fotoPeople =  new PeopleTakeFotoAsync(imageFilePath,"3");
                new takePhotoAsync().execute(fotoPeople);

            } else if (fotoVehiculoMaletera){

                PeopleTakeFotoAsync fotoPeople =  new PeopleTakeFotoAsync(imageFilePath,"4");
                new takePhotoAsync().execute(fotoPeople);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private class takePhotoAsync extends AsyncTask<PeopleTakeFotoAsync, Void, PeopleTakeFotoAsync> {

        @Override
        protected PeopleTakeFotoAsync doInBackground(PeopleTakeFotoAsync... params) {


            PeopleTakeFotoAsync objFotoWork = params[0];
            int width=0,height=0;
            Bitmap bitmapOrig = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            //String pathRoot = Environment.getExternalStorageDirectory() + "/Solgis/People/";

            Log.e("File Acces:",objFotoWork.getImageFilePath());

            //Revisión poder de archivo:
            try {
                //String imageInSD = "/sdcard/UserImages/" + userImageName;
                bitmapOrig = BitmapFactory.decodeFile(objFotoWork.getImageFilePath());
                if(bitmapOrig == null){
                    bitmapOrig = BitmapFactory.decodeFile(objFotoWork.getImageFilePath(), options);

                    width = bitmapOrig.getWidth();
                    height = bitmapOrig.getHeight();
                }
                else{
                    width = bitmapOrig.getWidth();
                    height = bitmapOrig.getHeight();
                }

                //return bitmap;
            } catch (Exception e) {

            }

            //adjust for camera orientation
            /*
            Bitmap bitmapOrig = BitmapFactory.decodeFile(objFotoWork.getImageFilePath());
            width = bitmapOrig.getWidth();
            height = bitmapOrig.getHeight();
            */

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
            File file2 = new File(objFotoWork.getImageFilePath());

            try {
                FileOutputStream out = new FileOutputStream(file2);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                objFotoWork.setSuccess(true);

                try{
                    switch (objFotoWork.getTipoFoto()){
                        case "1":
                            try {
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE People SET fotoValor = '"+objFotoWork.getImageFilePath()+"'");
                                dba.close();
                                Log.e("fotoValor ","true");
                            } catch (Exception eew){}
                            break;
                        case "2":

                            try {
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE People SET fotoVehiculo = '"+objFotoWork.getImageFilePath()+"'");
                                dba.close();
                                Log.e("fotoVehiculo ","true");
                            } catch (Exception eew){}

                            break;
                        case "3":

                            try {
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE People SET fotoVehiculoGuantera = '"+objFotoWork.getImageFilePath()+"'");
                                dba.close();
                                Log.e("fotoValorGuantera ","true");
                            } catch (Exception eew){}

                            break;
                        case "4":

                            try {
                                DBHelper dbHelperAlarm = new DBHelper(mContext);
                                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                                dba.execSQL("UPDATE People SET fotoVehiculoMaletera = '"+objFotoWork.getImageFilePath()+"'");
                                dba.close();
                                Log.e("fotoValorMaletera ","true");
                            } catch (Exception eew){}


                        default:

                            break;
                    }
                }
                catch (Exception e){

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(PeopleTakeFotoAsync result) {
            Log.e(TAG,"Result Post Execute");
            Log.e(TAG,result.toString());
            if(result.getSuccess()){
                //loadPrecinto();
                try{
                    switch (result.getTipoFoto()){
                        case "1":

                            btn_visualizar_valor.setVisibility(View.VISIBLE);
                            btn_visualizar_valor.setImageURI(Uri.parse(result.getImageFilePath()));
                            fotoValor = false;
                            btn_visualizar_valor.setEnabled(true);

                            break;
                        case "2":

                            btn_visualizar_vehiculo_delatera.setVisibility(View.VISIBLE);
                            btn_visualizar_vehiculo_delatera.setImageURI(Uri.parse(result.getImageFilePath()));
                            fotoVehiculo = false;
                            btn_visualizar_vehiculo_delatera.setEnabled(true);

                            break;
                        case "3":

                            btn_visualizar_vehiculo_guantera.setVisibility(View.VISIBLE);
                            btn_visualizar_vehiculo_guantera.setImageURI(Uri.parse(result.getImageFilePath()));
                            fotoVehiculoGuantera = false;
                            btn_visualizar_vehiculo_guantera.setEnabled(true);

                            break;

                        case "4":

                            btn_visualizar_vehiculo_maletera.setVisibility(View.VISIBLE);
                            btn_visualizar_vehiculo_maletera.setImageURI(Uri.parse(result.getImageFilePath()));
                            fotoVehiculoMaletera = false;
                            btn_visualizar_vehiculo_maletera.setEnabled(true);

                        default:
                            break;

                    }
                }
                catch (Exception e){

                }

            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
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
            String selectQuery = "SELECT dni, json, fotoValor, fotoVehiculo, fotoVehiculoGuantera, fotoVehiculoMaletera FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                dni = c.getString(c.getColumnIndex("dni"));
                json = c.getString(c.getColumnIndex("json"));
                fotoVal = c.getString(c.getColumnIndex("fotoValor"));
                fotoVeh = c.getString(c.getColumnIndex("fotoVehiculo"));
                fotoVehGuantera = c.getString(c.getColumnIndex("fotoVehiculoGuantera"));
                fotoVehMaletera = c.getString(c.getColumnIndex("fotoVehiculoMaletera"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Log.e("jsonObject ", String.valueOf(jsonObject));

        if (fotoVal==null && fotoVeh==null){
            casoSinNada(jsonObject);

        } else  if (fotoVal!=null && fotoVeh==null){
            casoConMaterial(jsonObject);

        } else  if (fotoVal==null && fotoVeh!=null){

            if (fotoVehGuantera == null){

                try {
                    if (pDialog != null && pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                } catch (Exception dsf){}

                Toast.makeText(mContext, "Falta foto de guantera", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fotoVehMaletera == null){
                try {
                    if (pDialog != null && pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                } catch (Exception dsf){}
                Toast.makeText(mContext, "Falta foto de maletera", Toast.LENGTH_SHORT).show();
                return;
            }

            casoConVehiculo(jsonObject);

        } else  if (fotoVal!=null && fotoVeh!=null){



            Log.e("DispositivoId ", GuidDipositivo);

            Log.e("Valor ", fotoVal);

            Log.e("Delantera ", fotoVeh);

            if (fotoVehGuantera == null){
                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                } catch (Exception dsf){}
                Toast.makeText(mContext, "Falta foto de guantera", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.e("fotoVehGuantera ", fotoVehGuantera);

            if (fotoVehMaletera == null){
                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                } catch (Exception dsf){}
                Toast.makeText(mContext, "Falta foto de maletera", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.e("fotoVehMaletera ", fotoVehMaletera);

            casoConMaterialVehiculoX(jsonObject);

            //casoConMaterialVehiculo(jsonObject);

        }

    }

    public void casoSinNada(JsonObject result){

        Log.e("SinNada ", "Ingreso");

        String URL = URL_API.concat("api/People/CreateAsync");
        CodigoSincronizacion = CodigoSincronizacion = UUID.randomUUID().toString();

        Log.e("DispositivoId ", GuidDipositivo);

        Ion.with(this)
                .load(URL)
                .setBodyParameter("DispositivoId", GuidDipositivo)
                .setBodyParameter("CodigoSincronizacion", CodigoSincronizacion)
                .setBodyParameter("codPers", result.get("codPers").getAsString())
                .setBodyParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setBodyParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setBodyParameter("codMotivo", result.get("codMotivo").getAsString())
                .setBodyParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setBodyParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setBodyParameter("codArea", result.get("codArea").getAsString())
                .setBodyParameter("nroPase", result.get("nroPase").getAsString())
                .setBodyParameter("persTipo", result.get("persTipo").getAsString())
                .setBodyParameter("peopleTipo", "0")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception fd){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
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

                                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}} catch (Exception edsv){}
                                try {showDialogSend();} catch (Exception e1) {e1.printStackTrace();}
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

    public void casoConMaterial(JsonObject result){

        Log.e("ConMaterial ", "Ingreso");

        String URL = URL_API.concat("api/People/CreateAsync");
        PeopleFotoCrud peopleFotoCrud = new PeopleFotoCrud(mContext);
        CodigoSincronizacion = CodigoSincronizacion = UUID.randomUUID().toString();
        //peopleFotoList =
        peopleFotoList =  peopleFotoCrud.insertConValor(CodigoSincronizacion,fotoVal);

        Ion.with(this)
                .load(URL)
                .setBodyParameter("DispositivoId", GuidDipositivo)
                .setBodyParameter("CodigoSincronizacion", CodigoSincronizacion)
                .setBodyParameter("codPers", result.get("codPers").getAsString())
                .setBodyParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setBodyParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setBodyParameter("codMotivo", result.get("codMotivo").getAsString())
                .setBodyParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setBodyParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setBodyParameter("codArea", result.get("codArea").getAsString())
                .setBodyParameter("nroPase", result.get("nroPase").getAsString())
                .setBodyParameter("persTipo", result.get("persTipo").getAsString())
                .setBodyParameter("peopleTipo", "1")
                //.setMultipartFile("Material", new File(fotoVal))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception fd){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
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

                                enviarFotosSingle(peopleFotoList);
                                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}} catch (Exception edsv){}
                                try {showDialogSend();} catch (Exception e1) {e1.printStackTrace();}
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

        Log.e("ConVehiculo ", "Ingreso");

        String URL = URL_API.concat("api/People/CreateAsync");
        PeopleFotoCrud peopleFotoCrud = new PeopleFotoCrud(mContext);
        CodigoSincronizacion = CodigoSincronizacion = UUID.randomUUID().toString();
        //peopleFotoList =
        peopleFotoList =  peopleFotoCrud.insertConVehiculo(CodigoSincronizacion,fotoVeh,fotoVehGuantera,fotoVehMaletera);

        Ion.with(this)
                .load(URL)
                .setBodyParameter("DispositivoId", GuidDipositivo)
                .setBodyParameter("CodigoSincronizacion", CodigoSincronizacion)
                .setBodyParameter("codPers", result.get("codPers").getAsString())
                .setBodyParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setBodyParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setBodyParameter("codMotivo", result.get("codMotivo").getAsString())
                .setBodyParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setBodyParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setBodyParameter("codArea", result.get("codArea").getAsString())
                .setBodyParameter("nroPase", result.get("nroPase").getAsString())
                .setBodyParameter("persTipo", result.get("persTipo").getAsString())
                .setBodyParameter("peopleTipo", "2")
                /*.setMultipartFile("Delantera", new File(fotoVeh))
                .setMultipartFile("Guantera", new File(fotoVehGuantera))
                .setMultipartFile("Maletera", new File(fotoVehMaletera))*/
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception fd){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
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
                                enviarFotosSingle(peopleFotoList);
                                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}} catch (Exception edsv){}
                                try {showDialogSend();} catch (Exception e1) {e1.printStackTrace();}
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

    public void casoConMaterialVehiculoX(JsonObject result){

        Log.e("MaterialVehiculoX ", "Ingreso");

        String cadenaA = "";

        String[] ary = GuidDipositivo.trim().split("-");

        for(int i=0;i<ary.length;i++){cadenaA = cadenaA +ary[i];}

        Log.e("-- valorEscaneado ", cadenaA);

        Log.e("DispositivoId ", cadenaA);
        Log.e("Material ", fotoVal);
        Log.e("Delantera ", fotoVeh);
        Log.e("Guantera ", fotoVehGuantera);
        Log.e("Maletera ", fotoVehMaletera);

        String URL = URL_API.concat("api/People/CreateAsync");
        //Log.e("URL ", URL);
        PeopleFotoCrud peopleFotoCrud = new PeopleFotoCrud(mContext);
        CodigoSincronizacion = CodigoSincronizacion = UUID.randomUUID().toString();
        //peopleFotoList =
        peopleFotoList =  peopleFotoCrud.insertAll(CodigoSincronizacion,fotoVal,fotoVeh,fotoVehGuantera,fotoVehMaletera);

        Ion.with(this)
                .load(URL)
                .setBodyParameter("DispositivoId", cadenaA)
                .setBodyParameter("CodigoSincronizacion", CodigoSincronizacion)
                .setBodyParameter("codPers", result.get("codPers").getAsString())
                .setBodyParameter("CodigoServicio", result.get("CodigoServicio").getAsString())
                .setBodyParameter("codMovSgte", result.get("codMovSgte").getAsString())
                .setBodyParameter("codMotivo", result.get("codMotivo").getAsString())
                .setBodyParameter("codEmpresa", result.get("codEmpresa").getAsString())
                .setBodyParameter("codAutoriX", result.get("codAutoriX").getAsString())
                .setBodyParameter("codArea", result.get("codArea").getAsString())
                .setBodyParameter("nroPase", result.get("nroPase").getAsString())
                .setBodyParameter("persTipo", result.get("persTipo").getAsString())
                .setBodyParameter("peopleTipo", "3")
                /*.setMultipartFile("Material", new File(fotoVal))
                .setMultipartFile("Delantera", new File(fotoVeh))
                .setMultipartFile("Guantera", new File(fotoVehGuantera))
                .setMultipartFile("Maletera", new File(fotoVehMaletera))*/
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception fd){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
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

                                enviarFotosSingle(peopleFotoList);

                                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}} catch (Exception edsv){}

                                try {showDialogSend();} catch (Exception e1) {e1.printStackTrace();}

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

    /*
    public void casoConMaterialVehiculo(JsonObject result){

        Log.e("ConMaterialVehiculo ", "Ingreso");

        String URL = URL_API.concat("api/People/Create");
        CodigoSincronizacion = UUID.randomUUID().toString();

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
                .setMultipartParameter("nroPase", "0")
                .setMultipartParameter("persTipo", result.get("persTipo").getAsString())
                .setMultipartFile("Material", new File(fotoVal))
                .setMultipartFile("Delantera", new File(fotoVeh))
                .setMultipartFile("Guantera", new File(fotoVehGuantera))
                .setMultipartFile("Maletera", new File(fotoVehMaletera))
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if(e != null){
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception fd){}

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
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
                        } else {

                            Log.e(".getHeaders() ", String.valueOf(response.getHeaders().code()));
                            Log.e(".getException() ", String.valueOf(response.getException()));
                            Log.e(".getResult() ", String.valueOf(response.getResult()));

                        }

                        try {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        } catch (Exception edsv){}

                    }
                });

    }
    */

    public void enviarFotosSingle(List<PeopleFoto> peopleFotos){

        new sendPhotoAsync().execute(peopleFotos);
    }

    private class sendPhotoAsync extends AsyncTask<List<PeopleFoto>, Void, List<PeopleFoto>> {

        @Override
        protected List<PeopleFoto> doInBackground(List<PeopleFoto>... params) {


            List<PeopleFoto> objFotoWork = params[0];
            PeopleFotoCrud peopleFotoCrud = new PeopleFotoCrud(mContext);

            try {
                DBHelper dataBaseHelper = new DBHelper(mContext);
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


            for (PeopleFoto peopleFoto : objFotoWork) {

                File archivoFoto = new File(peopleFoto.filePath);

                if(archivoFoto.isFile()){

                    //
                    String URL = URL_API.concat("api/People/SincronizacionFoto");

                    Log.e("Numero", Numero);
                    Log.e("DispositivoId", DispositivoId);
                    Log.e("CodSincro", peopleFoto.codigoSincronizacion);
                    Log.e("Tipo Foto", String.valueOf(peopleFoto.tipoFoto));
                    Log.e("File Path", peopleFoto.filePath);
                    Log.e("Indice", peopleFoto.indice);

                    Ion.with(mContext)
                            .load(URL)
                            .uploadProgressHandler(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                                }
                            })
                            .setTimeout(TIME_OUT)
                            .setMultipartParameter("DispositivoId", DispositivoId)
                            .setMultipartParameter("CodigoSincronizacion", peopleFoto.codigoSincronizacion)
                            .setMultipartParameter("TipoFoto", String.valueOf(peopleFoto.tipoFoto))
                            .setMultipartParameter("Id", String.valueOf(peopleFoto.peopleFotoId))
                            .setMultipartParameter("Indice", peopleFoto.indice)
                            .setMultipartFile("file", new File(peopleFoto.filePath))
                            //.setMultipartFile("Panoramica", new File(Panoramica))
                            .asString()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<String>>() {
                                @Override
                                public void onCompleted(Exception e, Response<String> response) {

                                    if(response.getHeaders().code()==200){

                                        Gson gson = new Gson();
                                        JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                        Log.e("JsonObject ", result.toString());

                                        if (result.get("Estado").getAsBoolean()){

                                            peopleFotoCrud.removePeopleFoto(peopleFoto);

                                            File file = new File(peopleFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    peopleFotoCrud.removePeopleFoto(peopleFoto);
                }


            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<PeopleFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }


    public void showDialogSend() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¡Registro Guardado!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                limpiarDatos();


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

    @Override
    protected void onDestroy() {

        try {
            DBHelper dbHelperAlarm = new DBHelper(this);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE People SET dni = "+null+", json = "+null+", fotoVehiculo = "+null+"," +
                    " fotoVehiculoGuantera = "+null+", fotoVehiculoMaletera = "+null+", fotoValor = "+null+"");
            dba.close();

        } catch (Exception edc){
            Log.e("EXCEPTION  ", " DESOTROY");
        }
        Log.e("ON  ", " DESOTROY");

        super.onDestroy();
    }

    public boolean limpiarDatos(){

        try {
            DBHelper dbHelperAlarm = new DBHelper(this);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE People SET dni = "+null+", json = "+null+", fotoVehiculo = "+null+"," +
                    " fotoVehiculoGuantera = "+null+", fotoVehiculoMaletera = "+null+", fotoValor = "+null+"");
            dba.close();
            Log.e("CONSULTA  ", " limpiarDatos");
        } catch (Exception edc){
            Log.e("EXCEPTION  ", " limpiarDatos");
        }
        Log.e("ON  ", " limpiarDatos");

        Intent i = new Intent(PeopleDetalle.this, People.class);
        startActivity(i);
        finish();

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

    public void peopleFoto(View view){

        String json = null, dni = null, fotoValor = null, fotoVehiculoA = null
                , fotoVehiculoGuanteraA = null, fotoVehiculoMaleteraA = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT json FROM People";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                json = c.getString(c.getColumnIndex("json"));

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (json == null){return;}

        if (json == null){return;}

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (!jsonObject.get("Img").isJsonNull()){
            visualizarImagenX(jsonObject.get("Img").getAsString());
        }

    }

    public void visualizarImagen(String uri){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PeopleDetalle.this);
        mView = getLayoutInflater().inflate(R.layout.popup_visualizacion, null);
        mBuilder.setCancelable(false);

        ImageView img = (ImageView) mView.findViewById(R.id.popup_img_visualizacion);

        if (uri==null){

            Toast.makeText(mContext, "¡No hay imagen para mostrar!", Toast.LENGTH_SHORT).show();
            return;
        }

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

                    try {

                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();

                        if(indice==1){
                            dba.execSQL("UPDATE People SET fotoValor = " + null);
                        } else if(indice==2){
                            dba.execSQL("UPDATE People SET fotoVehiculo = " + null);
                        } else if (indice==3){
                            dba.execSQL("UPDATE People SET fotoVehiculoGuantera = " + null);
                        } else if (indice==4){
                            dba.execSQL("UPDATE People SET fotoVehiculoMaletera = " + null);
                        }

                        dba.close();

                    } catch (Exception eew){}


                    loadDatosPeople();

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

    public void visualizarImagenX(String uri){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PeopleDetalle.this);
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

    public void mensajeTimeOut(){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PeopleDetalle.this);
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
