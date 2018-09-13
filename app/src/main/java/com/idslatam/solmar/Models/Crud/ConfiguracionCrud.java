package com.idslatam.solmar.Models.Crud;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.DTO.Configuracion.ConfiguracionSingleDTO;

public class ConfiguracionCrud {
    private DBHelper dbHelper;

    public ConfiguracionCrud(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public ConfiguracionSingleDTO getConfiguracion(){

        String DispositivoId="",Numero="";

        SQLiteDatabase dbConfiguration = dbHelper.getReadableDatabase();

        String selectQueryconfiguration = "SELECT GuidDipositivo,NumeroCel FROM Configuration";
        Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

        if (cConfiguration.moveToFirst()) {
            DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            Numero = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));

        }
        cConfiguration.close();
        //dbConfiguration.close();

        ConfiguracionSingleDTO objReturn = new ConfiguracionSingleDTO(DispositivoId,Numero);

        return  objReturn;
    }
}
