package com.idslatam.solmar.Patrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Cargo.Precinto.PrecintoCustomAdapter;
import com.idslatam.solmar.Cargo.Precinto.PrecintoDataModel;
import com.idslatam.solmar.Models.Crud.PatrolContenedorCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.PatrolContenedor;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Bienvenido;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ListadoContenedor extends AppCompatActivity {

    // List view
    private ListView lv;

    // Listview Adapter
    ArrayAdapter<String> adapter;

    Context mContext;

    String sUsername,ClienteMaterialId="",ClienteMaterialNombre="";

    // Search EditText
    EditText inputSearch;

    TextView titlePatrolList;

    String URL_API, ContenedorId;

    ArrayList<String> itemsList = new ArrayList<String>();

    boolean isProgress = false,TamanioObligatorio;

    int _PatrolContenedor_Id = 0, contadorLista,TamanioMaterial=0;

    ArrayList<PrecintoDataModel> dataModelsMovil;

    PrecintoCustomAdapter adapterMovil;

    InputFilter filterLenght = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_contenedor);

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        titlePatrolList = (TextView)findViewById(R.id.titlePatrolList);

        Intent intent = getIntent();

        ClienteMaterialId = intent.getStringExtra("ClienteMaterialId");
        ClienteMaterialNombre = intent.getStringExtra("ClienteMaterialNombre");
        TamanioMaterial = intent.getIntExtra("TamanioMaterial",0);
        TamanioObligatorio = intent.getBooleanExtra("TamanioObligatorio",false);

        titlePatrolList.setText("Buscar "+ClienteMaterialNombre);

        Log.e("ClienteMaterialId",ClienteMaterialId);

        dataModelsMovil = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list_view);

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo FROM PatrolContenedor WHERE ClienteMaterialId = '"+ClienteMaterialId+"' COLLATE NOCASE";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            contadorLista = c.getCount();
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (contadorLista==0){
            listaContenedores();

        } else {
            loadLista();
        }

        adapterMovil= new PrecintoCustomAdapter(dataModelsMovil,getApplicationContext());

        inputSearch = (EditText) findViewById(R.id.inputSearch);

        //Agregar restricciones para input Search:

        if(Integer.valueOf(TamanioMaterial)>0){
            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(TamanioMaterial));
        }
        else{
            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(50));
        }
        inputSearch.setFilters(new InputFilter[] { filterLenght });

        // Adding items to listview
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_contenedor, R.id.product_name, itemsList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                seleccion(String.valueOf(parent.getItemAtPosition(position)));
                //Toast.makeText(ListadoContenedor.this, "parent "+ parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

            }});
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ListadoContenedor.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });

    }

    public boolean loadLista(){

        //Log.e("Load","Inicia");
        itemsList.clear();

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo FROM PatrolContenedor WHERE ClienteMaterialId = '"+ClienteMaterialId+"' COLLATE NOCASE";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                do {
                    itemsList.add(c.getString(c.getColumnIndex("Codigo")));
                } while (c.moveToNext());
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        adapter = new ArrayAdapter<String>(this, R.layout.list_item_contenedor, R.id.product_name, itemsList);
        lv.setAdapter(adapter);

        return  true;
    }

    public synchronized void refreshAdapter() {

        if (itemsList != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void listaContenedores(){

        if (isProgress){return;}

        isProgress = true;

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(ListadoContenedor.this);
        pDialog.setMessage("Obteniendo Listado...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        try {

            DBHelper dbgelperDeete = new DBHelper(this);
            SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
            sqldbDelete.execSQL("DELETE FROM PatrolContenedor ");
            sqldbDelete.close();

        } catch (Exception e){}

        String DispositivoId = null;

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

        } catch (Exception e) {}


        String URL = URL_API.concat("api/Contenedor/GetAllMaterial?DispositivoId="+DispositivoId+"&ClienteMaterialId="+ClienteMaterialId);

        Log.e("URL MAtCont", URL);

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
                            } catch (Exception dslnkg){}

                            return;

                        }

                        if (response.getHeaders().code() == 200) {

                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                            Log.e("JsonObject Listado ", result.toString());

                            if (result.get("Data").isJsonNull()){

                                Log.e("JsonObject Data ", "NULL");

                                try {if (pDialog != null && pDialog.isShowing()) {pDialog.dismiss();}
                                } catch (Exception dslnkg){}

                                MensajeContenedor();

                                return;
                            }

                            JsonArray jsonArrayData = result.get("Data").getAsJsonArray();

                            for (JsonElement p : jsonArrayData) {

                                JsonObject jsonObject1 = p.getAsJsonObject();

                                Log.e(" Codigo ", jsonObject1.get("Codigo").getAsString());

                                try {

                                    PatrolContenedorCrud patrolContenedorCrud = new PatrolContenedorCrud(mContext);

                                    PatrolContenedor patrolContenedor = new PatrolContenedor();
                                    patrolContenedor.ContenedorId = jsonObject1.get("ContenedorId").getAsString();
                                    patrolContenedor.Codigo = jsonObject1.get("Codigo").getAsString();
                                    patrolContenedor.ClienteMaterialId = jsonObject1.get("ClienteMaterialId").getAsString();
                                    patrolContenedor.PatrolContenedorId = _PatrolContenedor_Id;

                                    _PatrolContenedor_Id = patrolContenedorCrud.insert(patrolContenedor);

                                } catch (Exception esca) {esca.printStackTrace();}

                            }

                            loadLista();


                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception ezs){}



                        } else {

                            Log.e("ERROR CODE ", String.valueOf(response.getHeaders().code()));

                            try {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } catch (Exception ezs){}
                        }

                        isProgress = false;

                    }});

    }

    public void seleccion(String value) {


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo, ContenedorId FROM PatrolContenedor WHERE Codigo = '"+value.toString()+"'";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                ContenedorId = c.getString(c.getColumnIndex("ContenedorId"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        if (ContenedorId==null){return;}

        Log.e("Codigo ", value);
        Log.e("ContenedorId ", ContenedorId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Ha selecionado "+ClienteMaterialNombre+" "+value+" .¿Desea Continuar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase dbT = dataBaseHelper.getWritableDatabase();
                    dbT.execSQL("UPDATE Configuration SET ContenedorPatrol = '"+value+"'");
                    dbT.execSQL("UPDATE Configuration SET ContenedorId = '"+ContenedorId+"'");
                    dbT.close();

                } catch (Exception e){}

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

    public void Nuevo(View view){
        refreshAdapter();
        listaContenedores();
    }

    public void crearContenedor(View view){

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ListadoContenedor.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_patrol_crear_contenedor, null);

        EditText editText = (EditText) mView.findViewById(R.id.patrol_cod_contenedor);
        TextView cargo_title_failed = (TextView) mView.findViewById(R.id.cargo_title_failed);
        TextView txtLabelCrearBien = (TextView) mView.findViewById(R.id.txtLabelCrearBien);
        //texMje.setText(mensaje);

        cargo_title_failed.setText("Crear: "+ClienteMaterialNombre);
        txtLabelCrearBien.setText("Ingrese Código "+String.valueOf(TamanioMaterial)+" caracteres");
        sUsername = editText.getText().toString();

        if(Integer.valueOf(TamanioMaterial)>0){
            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(TamanioMaterial));
        }
        else{
            filterLenght = new InputFilter.LengthFilter(Integer.valueOf(50));
        }
        editText.setFilters(new InputFilter[] { filterLenght });

        try {

            mBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (editText.getText().toString().matches("")){

                        Toast.makeText(mContext, "Ingrese código de "+ClienteMaterialNombre, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {

                        if(TamanioObligatorio){

                            if (editText.getText().toString().length() != (TamanioMaterial)){
                                Toast.makeText(mContext, ClienteMaterialNombre+" debe tener "+String.valueOf(TamanioMaterial)+" caracteres", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else{
                                dialog.dismiss();
                                contenedorApi(editText.getText().toString());
                            }

                        }else{

                            /*
                            if (editText.getText().toString().length() != (TamanioMaterial)){
                                Toast.makeText(mContext, "Contenedor debe tener "+String.valueOf(TamanioMaterial)+" caracteres", Toast.LENGTH_SHORT).show();
                                return;
                            }*/
                            dialog.dismiss();
                            contenedorApi(editText.getText().toString());

                        }
                    }

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contenedorApi(String codigoContenedor){

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(ListadoContenedor.this);
        pDialog.setMessage("Creando "+ClienteMaterialNombre+" ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        String DispositivoId = null;

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

        } catch (Exception e) {}


        Log.e(" Codigo ", codigoContenedor);
        Log.e(" GuidDipositivo ", DispositivoId);
        Log.e(" ClienteMaterialId ", ClienteMaterialId);

        String URL = URL_API.concat("api/Contenedor/CreateMaterial");

        JsonObject json = new JsonObject();
        json.addProperty("DispositivoId", DispositivoId);
        json.addProperty("Codigo", codigoContenedor);
        json.addProperty("ClienteMaterialId", ClienteMaterialId);

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

                            Log.e("Contendedor  Patrol ", result.toString());

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                            Toast.makeText(mContext, "¡Guardado Correctamente!", Toast.LENGTH_SHORT).show();

                            itemsList.clear();
                            listaContenedores();


                        } else {

                            Log.e("ERROR CODE ", String.valueOf(response.getHeaders().code()));

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                            Toast.makeText(mContext, "Error al registrar. ¡Intente Nuevamente!", Toast.LENGTH_LONG).show();
                        }
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }});
    }

    public void MensajeContenedor() {

        View mView;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ListadoContenedor.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_dni_patrol_ok, null);

        TextView txtTitle = (TextView) mView.findViewById(R.id.cargo_title_failed);
        TextView texMje = (TextView) mView.findViewById(R.id.cargo_mje_failed);

        txtTitle.setText("¡INFORMACIÓN!");
        texMje.setText("La lista de contenedores se encuentavacía.");

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

    public void retornaPatrol(View view){
        //Intent intent = new Intent(ListadoContenedor.this, PatrolActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        //startActivity(intent);
        finish();
    }

}
