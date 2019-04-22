package com.idslatam.solmar.People;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.ImageClass.ImageConverter;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.DTO.People.AreaAcceso.AreaAccesoItemDTO;
import com.idslatam.solmar.Models.Entities.DTO.People.AreaAcceso.AreaAccesoTableDTO;
import com.idslatam.solmar.People.AreaAcceso.AreaAccesoCustomPagerAdapter;
import com.idslatam.solmar.R;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.bitmap.Transform;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PeopleFormDetalleActivity extends AppCompatActivity {
    Context mContext;
    LinearLayout people_detalle_mensaje, people_detalle_datos, people_detalle_cuarto,
            people_detalle_quinto, people_detalle_aux, people_detalle_sexto, people_detalle_septimo;

    TextView people_txt_mensaje, people_detalle_mensaje_error;

    EditText people_detalle_dni, people_edt_persTipo, people_detalle_nombre, people_detalle_empresa,
            people_detalle_motivo, people_detalle_codArea;

    String GuidDipositivo,imageFilePath,CodigoSincronizacion=null,Numero=null,DispositivoId = null,
            URL_API = null;

    Spinner spinAreaAcceso;
    private AreaAccesoCustomPagerAdapter adapterAreaAcceso;

    ImageView people_detalle_img;
    Button people_detalle_btn_registrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_form_detalle);

        final File newFile = new File(Environment.getExternalStorageDirectory() + "/Solgis/People");
        newFile.mkdirs();

        mContext = this;




        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        spinAreaAcceso = (Spinner) findViewById(R.id.spinAreaAcceso);

        people_detalle_mensaje = (LinearLayout)findViewById(R.id.people_detalle_mensaje);
        people_detalle_datos = (LinearLayout)findViewById(R.id.people_detalle_datos);
        people_detalle_cuarto = (LinearLayout)findViewById(R.id.people_detalle_cuarto);
        people_detalle_quinto = (LinearLayout)findViewById(R.id.people_detalle_quinto);

        people_detalle_sexto = (LinearLayout)findViewById(R.id.people_detalle_sexto);
        people_detalle_septimo = (LinearLayout)findViewById(R.id.people_detalle_septimo);

        people_detalle_aux = (LinearLayout)findViewById(R.id.people_detalle_aux);

        /*
        btn_visualizar_valor = (ImageButton)findViewById(R.id.btn_visualizar_valor);
        btn_visualizar_vehiculo_delatera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_delatera);
        btn_visualizar_vehiculo_guantera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_guantera);
        btn_visualizar_vehiculo_maletera = (ImageButton)findViewById(R.id.btn_visualizar_vehiculo_maletera);

        btn_visualizar_valor.setOnClickListener(this);
        btn_visualizar_vehiculo_delatera.setOnClickListener(this);
        btn_visualizar_vehiculo_guantera.setOnClickListener(this);
        btn_visualizar_vehiculo_maletera.setOnClickListener(this);
        */

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
        try{
            loadAreaAcceso();
        }
        catch (Exception e){

        }
    }

    //Carga inicial de controles:
    private void loadAreaAcceso() throws Exception{

        Log.e("Inicio:","Inicio de función de carga");

        Response<AreaAccesoTableDTO> status;
        String URL = URL_API.concat("api/AreaAcceso/Combos?DispositivoId="+DispositivoId);

        Log.e("URL:",URL);

        try{
            status = Ion.with(mContext)
                    .load("GET",URL)
                    .as(AreaAccesoTableDTO.class)
                    .withResponse()
                    .get(25, TimeUnit.SECONDS);

            Log.e("Result:",status.getResult().toString());

            if(status.getHeaders().code()==200){
                adapterAreaAcceso = new AreaAccesoCustomPagerAdapter(mContext,R.layout.spinner_text,status.getResult().data);

                for (AreaAccesoItemDTO item: status.getResult().data) {
                    Log.e("Data:"+item.getId(),item.getNombre());
                }

                spinAreaAcceso.setAdapter(adapterAreaAcceso);

                spinAreaAcceso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id){

                        AreaAccesoItemDTO areaAcceso = adapterAreaAcceso.getItem(position);

                        Toast.makeText(mContext, "ID: " + areaAcceso.getId() + "\nName: " + areaAcceso.getNombre(),
                                Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {  }
                });

            }



        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {

            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void loadDatosPeople(){

        /*
        btn_visualizar_valor.setVisibility(View.GONE);
        btn_visualizar_vehiculo_delatera.setVisibility(View.GONE);
        btn_visualizar_vehiculo_guantera.setVisibility(View.GONE);
        btn_visualizar_vehiculo_maletera.setVisibility(View.GONE);
        */

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {
            DispositivoId = null;
        }

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

        /*

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
        */

    }


}
