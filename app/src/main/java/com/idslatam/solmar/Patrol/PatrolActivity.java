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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.idslatam.solmar.Cargo.CargoActivity;
import com.idslatam.solmar.Models.Crud.PatrolPrecintoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;
import com.idslatam.solmar.Patrol.Contenedor.CustomAdapter;
import com.idslatam.solmar.Patrol.Contenedor.DataModel;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PatrolActivity extends AppCompatActivity {


    private static final int CAPTURE_MEDIA = 368;

    private Activity activity;

    Uri photoUri;

    int _PatrolPrecinto_Id = 0, contadorLista = 0;

    int count = 0;

    String ContenedorId, URL_API, DispositivoId, formato;

    Context mContext;

    GridView listView;

    ArrayList<DataModel> dataModelsMovil;

    CustomAdapter adapterMovil;

    TextView quinto_txt_nro_precintos;

    EditText edt_contenedor_seleccionado;


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

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPrecinto();
    }

    public void fotoPrecinto(View view){

        int ctaA = 0;
        try {
            DBHelper bdh = new DBHelper(this);
            SQLiteDatabase sqlite = bdh.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto";
            Cursor ca = sqlite.rawQuery(selectQuery, new String[]{});
            ctaA = ca.getCount();
            ca.close();
            sqlite.close();

        } catch (Exception e) {}


        Log.e("ctaA ", String.valueOf(ctaA));

        if (ctaA == 6){
            Toast.makeText(this, "¡Precintos Completos!", Toast.LENGTH_SHORT).show();
            return;
        }

        tomarFoto();
    }

    // METODOS DE CAMARA
    public void tomarFoto(){

        photoUri = null;

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

            if (contadorLista==0){
                contadorLista = 1;
            } else {
                contadorLista++;
            }

            try {

                PatrolPrecintoCrud patrolPrecintoCrud = new PatrolPrecintoCrud(mContext);
                PatrolPrecinto patrolPrecinto = new PatrolPrecinto();
                patrolPrecinto.Indice = "Precinto Nº " + String.valueOf(contadorLista);
                patrolPrecinto.Foto = photoUri;
                patrolPrecinto.PatrolPrecintoId = _PatrolPrecinto_Id;
                _PatrolPrecinto_Id = patrolPrecintoCrud.insert(patrolPrecinto);

                Log.e("isPrecinto  ", "fin");

            } catch (Exception esca) {esca.printStackTrace();}

            loadPrecinto();

            Log.e(" Position GUID ", String.valueOf(photoUri));
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void loadPrecinto(){

        String NroContenedor = null;

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
            contadorLista = c.getCount();
            if (c.moveToFirst()) {

                do {
                    dataModelsMovil.add(new DataModel(c.getString(c.getColumnIndex("Indice")),
                            c.getString(c.getColumnIndex("Foto"))));
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        adapterMovil= new CustomAdapter(dataModelsMovil,getApplicationContext());
        listView.setAdapter(adapterMovil);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DataModel datamo = dataModelsMovil.get(position);
                visualizarImagen(datamo.getUri());
            }
        });

        quinto_txt_nro_precintos.setText("Precintos: "+ String.valueOf(contadorLista) +" de 6");

    }

    public void listaContenedor(View view){
        Intent intent = new Intent(PatrolActivity.this, ListadoContenedor.class);
        startActivity(intent);
    }


    public void GuardarPatrol(View view){
        guardarPatrol();
    }

    @Override
    public void onBackPressed() {



            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("¿Está seguro que desea salir?");

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    borrarDatos();
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

        finish();
    }

    public void guardarPatrol(){

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(PatrolActivity.this);
        pDialog.setMessage("Registrando Patrol...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        String URL = URL_API.concat("api/Patrol/Create");

        List<Part> files = new ArrayList();

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Foto FROM PatrolPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                do {
                    files.add(new FilePart("Files", new File(c.getString(c.getColumnIndex("Foto")))));
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

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
                .setTimeout(15 * 60 * 1000)
                .addMultipartParts(files)
                .setMultipartParameter("ContenedorId", ContenedorId)
                .setMultipartParameter("DispositivoId", DispositivoId)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        if (e != null) {
                            Toast.makeText(mContext, "Error uploading file", Toast.LENGTH_LONG).show();
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
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
                                try {
                                    mensajeGuardado();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                //MensajeErrorPatrol();
                                if (!result.get("Exception").isJsonNull()){
                                    Toast.makeText(mContext, result.get("Exception").getAsString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        } else {
                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception edsv){}
                            Log.e("PATROL Error Code ", String.valueOf(response.getHeaders().code()));
                        }
                    }
                });
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

    public void MensajeErrorPatrol(){

        /*try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¡Ha ocurrido una excepción! " + );
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    borrarDatos();
                    dialog.dismiss();
                }
            });
            builder.show();

        } catch (Exception sdf){}*/
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
                        dbU.execSQL("DELETE FROM PatrolPrecinto WHERE Foto = '"+uri+"'");
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

}
