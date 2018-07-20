package com.idslatam.solmar.Patrol;

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
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.PatrolFotoCrud;
import com.idslatam.solmar.Models.Crud.PatrolPrecintoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.DTO.Patrol.PatrolPrecintoDBList;
import com.idslatam.solmar.Models.Entities.DTO.Patrol.PatrolTakeFotoAsync;
import com.idslatam.solmar.Models.Entities.PatrolFoto;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;
import com.idslatam.solmar.Patrol.Contenedor.CustomAdapter;
import com.idslatam.solmar.Patrol.Contenedor.DataModel;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PatrolActivity extends AppCompatActivity {


    private static final int CAPTURE_MEDIA = 368;

    private Activity activity;

    Uri photoUri;

    int _PatrolPrecinto_Id = 0, contadorLista = 0, TIME_OUT = 5 * 60 * 1000;

    int count = 0;

    String ContenedorId, URL_API, DispositivoId, numeroPrecintoFoto,imageFilePath,CodigoSincronizacion;

    Context mContext;

    GridView listView;

    ArrayList<DataModel> dataModelsMovil;

    CustomAdapter adapterMovil;

    TextView quinto_txt_nro_precintos;

    EditText edt_contenedor_seleccionado;

    int cantidadFotos, posicion;

    private static final String TAG = "PatrolAsync";
    private static final int REQUEST_CODE_PHOTO_TAKEN_ASYNC = 2;

    List<PatrolFoto> patrolFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrol);

        activity = this;
        mContext = this;

        quinto_txt_nro_precintos = (TextView) findViewById(R.id.quinto_txt_nro_precintos);
        edt_contenedor_seleccionado = (EditText)findViewById(R.id.edt_contenedor_seleccionado);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        final File newFile = new File(Environment.getExternalStorageDirectory() + "/Solgis/Patrol");
        newFile.mkdirs();


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPrecinto();
    }

    public void fotoPrecinto(String numeroPrecinto, int pos){

        numeroPrecintoFoto = numeroPrecinto;

        if (edt_contenedor_seleccionado.getText().toString().matches("")){
            Toast.makeText(mContext, "Falta contenedor", Toast.LENGTH_SHORT).show();
            return;
        }

       int ctaFotos = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            ctaFotos = c.getCount();
            c.close();
            dbst.close();
         } catch (Exception e) {}


        /*try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("UPDATE Configuration SET Indice = " + ctaFotos);

            if (ctaFotos >= pos){
                dbT.execSQL("UPDATE Configuration SET Posicion = " + pos);
            }

            dbT.close();

        } catch (Exception e){}

        int indAux = 0, posAux = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Indice, Posicion FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                indAux = c.getInt(c.getColumnIndex("Indice"));
                posAux = c.getInt(c.getColumnIndex("Posicion"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}*/


        Log.e("indAux P ", String.valueOf(ctaFotos));
        Log.e("posAux P ", String.valueOf(pos));

        cantidadFotos = ctaFotos;
        posicion = pos;

        if (ctaFotos==0 && pos == 0){
            tomarFoto();
            return;
        }

        if(ctaFotos == pos){
            tomarFoto();
        } else if (pos <= ctaFotos){
            tomarFoto();
        }else {
            Toast.makeText(activity, "Tomar fotos de precintos anteriores", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {

        Log.e(TAG,"Create file");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory()+"/Solgis/Patrol");//getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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

    public void tomarFoto(){
        Log.e(TAG,"Call Camera");
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
    /*
    // METODOSDECAMARA
    public void tomarFoto(){

        photoUri = null;

        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_LOWEST)
                .enableImageCropping(false)
                .launchCamera();

    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK){
            return;
        }

        if(resultCode == -1 && requestCode == REQUEST_CODE_PHOTO_TAKEN_ASYNC){
            //String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            PatrolTakeFotoAsync objFotoWork = new PatrolTakeFotoAsync(imageFilePath,numeroPrecintoFoto);

            new takePhotoAsync().execute(objFotoWork);

            //loadPrecinto();
        }

        /*
        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {

            String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);

            try {

                DBHelper dbHelperNumero = new DBHelper(this);
                SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                dbNro.execSQL("UPDATE PatrolPrecinto SET Foto = '"+photoUri+"' WHERE Indice = '"+numeroPrecintoFoto+"'");
                dbNro.close();
            } catch (Exception eew){}

            loadPrecinto();

            Log.e(" Position GUID ", String.valueOf(photoUri));
        }
        */

        super.onActivityResult(requestCode, resultCode, data);

    }

    private class takePhotoAsync extends AsyncTask<PatrolTakeFotoAsync, Void, PatrolTakeFotoAsync> {

        @Override
        protected PatrolTakeFotoAsync doInBackground(PatrolTakeFotoAsync... params) {


            PatrolTakeFotoAsync objFotoWork = params[0];

            String pathRoot = Environment.getExternalStorageDirectory() + "/Solgis/Patrol/";

            //adjust for camera orientation
            Bitmap bitmapOrig = BitmapFactory.decodeFile(objFotoWork.getImageFilePath());
            int width = bitmapOrig.getWidth();
            int height = bitmapOrig.getHeight();

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

                try {

                    DBHelper dbHelperNumero = new DBHelper(mContext);
                    SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                    dbNro.execSQL("UPDATE PatrolPrecinto SET Foto = '"+imageFilePath+"' WHERE Indice = '"+numeroPrecintoFoto+"'");
                    dbNro.close();

                    objFotoWork.setSuccess(true);
                } catch (Exception eew){}

            } catch (Exception e) {
                e.printStackTrace();
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(PatrolTakeFotoAsync result) {
            Log.e(TAG,"Result Post Execute");
            Log.e(TAG,result.toString());
            if(result.getSuccess()){
                loadPrecinto();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public void loadPrecinto(){

        String NroContenedor = null;
        int contadorIndice = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Indice FROM PatrolPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorIndice = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contadorIndice==0){

            for (int i = 1; i <= 6 ; i++){

                try {
                    PatrolPrecintoCrud patrolPrecintoCrud = new PatrolPrecintoCrud(mContext);
                    PatrolPrecinto patrolPrecinto = new PatrolPrecinto();
                    patrolPrecinto.Indice = "Precinto No " + String.valueOf(i);
                    patrolPrecinto.PatrolPrecintoId = _PatrolPrecinto_Id;
                    _PatrolPrecinto_Id = patrolPrecintoCrud.insert(patrolPrecinto);
                } catch (Exception esca) {esca.printStackTrace();}

            }
        }


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT ContenedorPatrol FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                NroContenedor = c.getString(c.getColumnIndex("ContenedorPatrol"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        if (NroContenedor == null){
            edt_contenedor_seleccionado.setText("");
        } else {
            edt_contenedor_seleccionado.setText(NroContenedor);
        }

        dataModelsMovil = new ArrayList<>();

        listView = (GridView) findViewById(R.id.quinto_list_fotos);

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Indice, Foto FROM PatrolPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                do {
                    dataModelsMovil.add(new DataModel(c.getString(c.getColumnIndex("Indice")),
                            c.getString(c.getColumnIndex("Foto"))));
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        adapterMovil= new CustomAdapter(dataModelsMovil,getApplicationContext());
        listView.setAdapter(adapterMovil);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DataModel datamo = dataModelsMovil.get(position);

                if (datamo.getUri()==null){
                    fotoPrecinto(datamo.getName(), position);
                } else {
                    visualizarImagen(datamo.getUri());
                }
            }
        });

        quinto_txt_nro_precintos.setText("Precintos: "+ String.valueOf(contadorLista) +" de 6");

    }

    public void listaContenedor(View view){

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("DELETE FROM sqlite_sequence WHERE NAME ='PatrolPrecinto'");
            dbT.close();

        } catch (Exception edsv){}

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("DELETE FROM PatrolPrecinto");
            dbT.close();

        } catch (Exception e){}

        Intent intent = new Intent(PatrolActivity.this, ListadoContenedor.class);
        startActivity(intent);
    }

    public void GuardarPatrol(View view){

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            cantidadFotos = c.getCount();
            c.close();
            dbst.close();
        } catch (Exception e) {}

        int ultimaposi = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT PatrolPrecintoId FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToLast()) {
                ultimaposi = c.getInt(c.getColumnIndex("PatrolPrecintoId"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        Log.e("cantidadFotos G ", String.valueOf(cantidadFotos));
        Log.e("ultimaposi G ", String.valueOf((ultimaposi)));

        if ( cantidadFotos != ultimaposi){
            Toast.makeText(activity, "¡Por favor completar fotos!", Toast.LENGTH_SHORT).show();
            return;
        }

        guardarPatrol();
    }

    @Override
    public void onBackPressed() {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("¿Está seguro que desea salir?");

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {

                        DBHelper dataBaseHelper = new DBHelper(mContext);
                        SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                        dbT.execSQL("DELETE FROM sqlite_sequence WHERE NAME ='PatrolPrecinto'");
                        dbT.close();

                    } catch (Exception edsv){}

                    try {

                        DBHelper dataBaseHelper = new DBHelper(mContext);
                        SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                        dbT.execSQL("UPDATE Configuration SET Posicion = 0");
                        dbT.close();

                    } catch (Exception edsv){}
                    borrarDatos();
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

    public void borrarDatos(){

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("UPDATE Configuration SET ContenedorPatrol = " + null);
            dbT.close();

        } catch (Exception e){}

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("DELETE FROM PatrolPrecinto");
            dbT.close();

        } catch (Exception e){}

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
            dbT.execSQL("DELETE FROM sqlite_sequence WHERE NAME ='PatrolPrecinto'");
            dbT.close();

        } catch (Exception edsv){}

        finish();
    }

    public void guardarPatrol(){

        int contador = 0;

        if (edt_contenedor_seleccionado.getText().toString().matches("")){
            Toast.makeText(mContext, "¡Falta Contenedor!", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contador = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contador == 0){
            Toast.makeText(mContext, "¡Falta precintos!", Toast.LENGTH_SHORT).show();
            return;
        }


        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(PatrolActivity.this);
        pDialog.setMessage("Registrando Patrol...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Patrol/CreateAsync");
        CodigoSincronizacion = UUID.randomUUID().toString();

        int _in = 0;

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto WHERE Foto IS NOT NULL";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            List<PatrolPrecintoDBList> _patrolPrecintoDBLists = new ArrayList<>();

            if (c.moveToFirst()) {

                do {
                    //files.add(new FilePart("Files", new File(c.getString(c.getColumnIndex("Foto")))));
                    PatrolPrecintoDBList _patrolPrecintoDBList = new PatrolPrecintoDBList(c.getString(c.getColumnIndex("Foto")),_in);
                    _patrolPrecintoDBLists.add(_patrolPrecintoDBList);
                    _in++;

                } while (c.moveToNext());

            }

            c.close();
            dbst.close();

            PatrolFotoCrud patrolFotoCrud = new PatrolFotoCrud(mContext);
            patrolFotos =  patrolFotoCrud.insertFotosPatrol(CodigoSincronizacion,_patrolPrecintoDBLists);

        } catch (Exception e) {}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT GuidDipositivo, ContenedorId FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                ContenedorId = c.getString(c.getColumnIndex("ContenedorId"));
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Log.e("ContenedorId ", ContenedorId);
        Log.e("DispositivoId ", DispositivoId);

        Ion.with(mContext)
                .load(URL)
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long total) {
                        Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                    }
                })
                .setTimeout(TIME_OUT)
                .setLogging("PATRO_ION", Log.DEBUG)
                //.addMultipartParts(files)
                .setBodyParameter("ContenedorId", ContenedorId)
                .setBodyParameter("DispositivoId", DispositivoId)
                .setBodyParameter("CodigoSincronizacion", CodigoSincronizacion)
                .setBodyParameter("NumeroPrecintos", String.valueOf(_in))
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

                            Log.e("Excepction ContVacio", e.toString());

                            if (e.toString().equalsIgnoreCase("java.util.concurrent.TimeoutException")){
                                mensajeTimeOut();
                            }

                            return;
                        }

                        if(response.getHeaders().code()==200){

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject PATROL", result.toString());

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}

                            if (result.get("Estado").getAsBoolean()){

                                enviarFotosSingle(patrolFotos);

                                try {

                                    DBHelper dataBaseHelper = new DBHelper(mContext);
                                    SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                                    dbT.execSQL("UPDATE Configuration SET Posicion = 0");
                                    dbT.close();

                                } catch (Exception edsv){}

                                try {
                                    mensajeGuardado();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                //
                                if (!result.get("Exception").isJsonNull()){
                                    MensajeErrorPatrol(result.get("Exception").getAsString());
                                }

                            }
                        } else {
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                            MensajeErrorPatrol(String.valueOf(response.getHeaders().code()));
                            Log.e("PATROL Error Code ", String.valueOf(response.getHeaders().code()));
                        }
                    }
                });
    }

    public void enviarFotosSingle(List<PatrolFoto> patrolFotos){

        new sendPhotoAsync().execute(patrolFotos);
    }

    private class sendPhotoAsync extends AsyncTask<List<PatrolFoto>, Void, List<PatrolFoto>> {

        @Override
        protected List<PatrolFoto> doInBackground(List<PatrolFoto>... params) {


            List<PatrolFoto> objFotoWork = params[0];
            PatrolFotoCrud patrolFotoCrud = new PatrolFotoCrud(mContext);

            try {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT GuidDipositivo, NumeroCel FROM Configuration";
                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {
                    //Numero = c.getString(c.getColumnIndex("NumeroCel"));
                    DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                }
                c.close();
                dbst.close();

            } catch (Exception e) {}


            for (PatrolFoto patrolFoto : objFotoWork) {

                File archivoFoto = new File(patrolFoto.filePath);

                if(archivoFoto.isFile()){

                    //
                    String URL = URL_API.concat("api/Patrol/SincronizacionFoto");

                    //Log.e("Numero", Numero);
                    Log.e("DispositivoId", DispositivoId);
                    Log.e("CodigoSincro", patrolFoto.codigoSincronizacion);
                    //Log.e("Tipo Foto", String.valueOf(cargoFoto.tipoFoto));
                    Log.e("File Path", patrolFoto.filePath);
                    Log.e("Indice", patrolFoto.indice);

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
                            .setMultipartParameter("CodigoSincronizacion", patrolFoto.codigoSincronizacion)
                            .setMultipartParameter("Id", String.valueOf(patrolFoto.patrolFotoId))
                            .setMultipartParameter("Indice", patrolFoto.indice)
                            .setMultipartFile("file", new File(patrolFoto.filePath))
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

                                            patrolFotoCrud.removePatrolFoto(patrolFoto);

                                            File file = new File(patrolFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    patrolFotoCrud.removePatrolFoto(patrolFoto);
                }
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<PatrolFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public void mensajeGuardado(){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¡Patrol Guardado!");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    borrarDatos();
                    dialog.dismiss();
                }
            });
            builder.show();

        } catch (Exception sdf){}
    }

    public void MensajeErrorPatrol(String mensajeError){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¡Ha ocurrido una excepción!");
            builder.setMessage(mensajeError);
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    borrarDatos();
                    dialog.dismiss();
                }
            });
            builder.show();

        } catch (Exception sdf){}
    }

    public void visualizarImagen(String uri){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PatrolActivity.this);
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

                    try {
                        DBHelper dataBaseHelperB = new DBHelper(mContext);
                        SQLiteDatabase dbU = dataBaseHelperB.getWritableDatabase();
                        //dbU.execSQL("DELETE FROM CargoPrecinto WHERE Foto = '"+uri+"'");
                        dbU.execSQL("UPDATE PatrolPrecinto SET Foto = " + null+" WHERE Foto = '"+uri+"'");
                        dbU.close();

                    } catch (Exception e){}


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

    public void mensajeTimeOut(){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PatrolActivity.this);
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
