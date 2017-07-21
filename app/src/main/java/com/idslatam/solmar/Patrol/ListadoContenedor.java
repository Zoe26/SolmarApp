package com.idslatam.solmar.Patrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
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

public class ListadoContenedor extends AppCompatActivity {

    // List view
    private ListView lv;

    // Listview Adapter
    ArrayAdapter<String> adapter;

    Context mContext;

    // Search EditText
    EditText inputSearch;

    String URL_API, ContenedorId;

    ArrayList<String> itemsList = new ArrayList<String>();

    ListView listView;

    int _PatrolContenedor_Id = 0, contadorLista;

    ArrayList<PrecintoDataModel> dataModelsMovil;

    PrecintoCustomAdapter adapterMovil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_contenedor);

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        /*for (int i = 0; i <= 10; i++){
            itemsList.add("Samsung "+i);
        }*/


        dataModelsMovil = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list_view);

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo FROM PatrolContenedor";
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

        // Adding items to listview
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_contenedor, R.id.product_name, itemsList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                seleccion(String.valueOf(parent.getItemAtPosition(position)));
                //Toast.makeText(ListadoContenedor.this, "parent "+ parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

            }});

        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ListadoContenedor.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    public void loadLista(){
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo FROM PatrolContenedor";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {

                do {

                    itemsList.add(c.getString(c.getColumnIndex("Codigo")));
                    //dataModelsMovil.add(new PrecintoDataModel(c.getString(c.getColumnIndex("Codigo")),
                    //        c.getString(c.getColumnIndex("Foto"))));
                } while (c.moveToNext());

            }
            c.close();
            dbst.close();

        } catch (Exception e) {}
    }

    public void listaContenedores(){

        String URL = URL_API.concat("api/Contenedor/GetAll");

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(ListadoContenedor.this);
        pDialog.setMessage("Obteniendo Listado...");
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

                            Log.e("JsonObject Listado ", result.toString());

                            JsonArray jsonArrayData = result.get("Data").getAsJsonArray();

                            for (JsonElement p : jsonArrayData) {

                                JsonObject jsonObject1 = p.getAsJsonObject();

                                try {

                                    PatrolContenedorCrud patrolContenedorCrud = new PatrolContenedorCrud(mContext);
                                    PatrolContenedor patrolContenedor = new PatrolContenedor();
                                    patrolContenedor.ContenedorId = jsonObject1.get("ContenedorId").getAsString();
                                    patrolContenedor.Codigo = jsonObject1.get("Codigo").getAsString();
                                    patrolContenedor.PatrolContenedorId = _PatrolContenedor_Id;
                                    _PatrolContenedor_Id = patrolContenedorCrud.insert(patrolContenedor);

                                } catch (Exception esca) {esca.printStackTrace();}

                            }

                            loadLista();


                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }


                        } else {

                            Log.e("ERROR CODE ", String.valueOf(response.getHeaders().code()));

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }

                    }});
    }

    public void seleccion(String value) {


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Codigo, ContenedorId FROM PatrolContenedor WHERE Codigo = "+value;
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                ContenedorId = c.getString(c.getColumnIndex("ContenedorId"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}

        Log.e("Codigo ", value);
        Log.e("ContenedorId ", ContenedorId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Ha selecionado el contenedor "+value+" .¿Desea Continuar?");
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

}