package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.Menu;

/**
 * Created by desarrollo03 on 6/4/17.
 */

public class CargoCrud {

    private DBHelper dbHelper;

    public CargoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Cargo cargo) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Cargo.KEY_Initial, cargo.Initial);
        values.put(Cargo.KEY_TipoCarga, cargo.TipoCarga);
        values.put(Cargo.KEY_isLicencia, cargo.isLicencia);
        values.put(Cargo.KEY_isCarga, cargo.isCarga);
        values.put(Cargo.KEY_EppCasco, cargo.EppCasco);
        values.put(Cargo.KEY_EppChaleco, cargo.EppChaleco);
        values.put(Cargo.KEY_EppBotas, cargo.EppBotas);
        values.put(Cargo.KEY_tamanoContenedor, cargo.tamanoContenedor);
        values.put(Cargo.KEY_tipoDocumento, cargo.tipoDocumento);


        long CargoId = db.insert(Cargo.TABLE_CARGO, null, values);
        db.close();
        return (int) CargoId;

    }

    public int updatePlaca(String placa,int Id){

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Cargo.KEY_Placa, placa);

        db.update(Cargo.TABLE_CARGO,values,Cargo.KEY_ID_Cargo+" = "+String.valueOf(Id),null);

        return Id;
    }

    public int updateTipoCarga(String TipoCargaId,int Id){

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Cargo.KEY_TipoCarga, TipoCargaId);

        db.update(Cargo.TABLE_CARGO,values,Cargo.KEY_ID_Cargo+" = "+String.valueOf(Id),null);

        return Id;
    }

    public int updateFieldGeneric(String Field,String ValueField,int Id){

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Field, ValueField);

        db.update(Cargo.TABLE_CARGO,values,Cargo.KEY_ID_Cargo+" = "+String.valueOf(Id),null);

        return Id;
    }

    public Cargo getCargo(){
        Cargo cargo = new Cargo();

        SQLiteDatabase dbst = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT Placa, TipoCarga, Dni, isIngreso, numeroPrecintos,isCarga," +
                " numeroDocumento, CantidadBultos, EppCasco, EppChaleco, EppBotas," +
                " isLicencia,Alcolimetro, json, UpdateTipoCarga, codigoContenedor, "+
                " Carreta, numeroPrecintos, pv, isCargaVerificada, tamanoContenedor, GuiaRemision, "+
                " OrigenId, DestinoId, TipoCargaForFotos"+
                " FROM Cargo";
        Cursor c = dbst.rawQuery(selectQuery, new String[]{});
        if (c.moveToFirst()) {
            //Placa = c.getString(c.getColumnIndex("Placa"));
            /*TipoCarga = c.getString(c.getColumnIndex("TipoCarga"));
            Dni = c.getString(c.getColumnIndex("Dni"));
            isIngreso = c.getString(c.getColumnIndex("isIngreso"));
            */
            cargo.setPlaca(c.getString(c.getColumnIndex("Placa")));
            cargo.setDni(c.getString(c.getColumnIndex("Dni")));
            cargo.setTipoCarga(c.getString(c.getColumnIndex("TipoCarga")));
            cargo.setTipoCargaForFotos(c.getString(c.getColumnIndex("TipoCargaForFotos")));
            cargo.setUpdateTipoCarga(c.getString(c.getColumnIndex("UpdateTipoCarga")));
            //
            cargo.setIsIngreso(c.getString(c.getColumnIndex("isIngreso")));
            cargo.setEppCasco(c.getString(c.getColumnIndex("EppCasco")));
            cargo.setEppChaleco(c.getString(c.getColumnIndex("EppChaleco")));
            cargo.setEppBotas(c.getString(c.getColumnIndex("EppBotas")));
            cargo.setIsLicencia(c.getString(c.getColumnIndex("isLicencia")));
            cargo.setAlcolimetro(c.getString(c.getColumnIndex("Alcolimetro")));
            cargo.setJson(c.getString(c.getColumnIndex("json")));

            //Carga:
            cargo.setCarreta(c.getString(c.getColumnIndex("Carreta")));
            cargo.setOrigenId(c.getString(c.getColumnIndex("OrigenId")));
            cargo.setDestinoId(c.getString(c.getColumnIndex("DestinoId")));
            cargo.setNumeroPrecintos(c.getString(c.getColumnIndex("numeroPrecintos")));
            cargo.setIsCarga(c.getString(c.getColumnIndex("isCarga")));
            cargo.setNumeroDocumento(c.getString(c.getColumnIndex("numeroDocumento")));
            cargo.setPv(c.getString(c.getColumnIndex("pv")));
            cargo.setCantidadBultos(c.getString(c.getColumnIndex("CantidadBultos")));
            cargo.setIsCargaVerificada(c.getString(c.getColumnIndex("isCargaVerificada")));
            cargo.setCodigoContenedor(c.getString(c.getColumnIndex("codigoContenedor")));
            cargo.setTamanoContenedor(c.getString(c.getColumnIndex("tamanoContenedor")));
            cargo.setGuiaRemision(c.getString(c.getColumnIndex("GuiaRemision")));

        }
        c.close();
        dbst.close();

        return cargo;
    }


    public void allCargoNull(){
        //DBHelper dbHelperAlarm = new DBHelper(mContext);
        SQLiteDatabase dba = dbHelper.getWritableDatabase();
        dba.execSQL("UPDATE Cargo SET NroOR = " + null);

        //Persona
        dba.execSQL("UPDATE Cargo SET Placa = " + null);
        dba.execSQL("UPDATE Cargo SET TipoCarga = " + null);
        dba.execSQL("UPDATE Cargo SET TipoCargaForFotos = " + null);
        dba.execSQL("UPDATE Cargo SET Dni = " + null);
        dba.execSQL("UPDATE Cargo SET json = " + null);
        dba.execSQL("UPDATE Cargo SET EppCasco = " + null);
        dba.execSQL("UPDATE Cargo SET EppChaleco = " + null);
        dba.execSQL("UPDATE Cargo SET EppBotas = " + null);
        dba.execSQL("UPDATE Cargo SET isLicencia = " + null);
        dba.execSQL("UPDATE Cargo SET Alcolimetro = " + null);

        //Carga:
        dba.execSQL("UPDATE Cargo SET Carreta = " + null);
        dba.execSQL("UPDATE Cargo SET isCarga = " + null);
        dba.execSQL("UPDATE Cargo SET OrigenId = " + null);
        dba.execSQL("UPDATE Cargo SET DestinoId = " + null);
        dba.execSQL("UPDATE Cargo SET tamanoContenedor = " + null);
        dba.execSQL("UPDATE Cargo SET codigoContenedor = " + null);
        dba.execSQL("UPDATE Cargo SET numeroPrecintos = " + null);
        dba.execSQL("UPDATE Cargo SET GuiaRemision = " + null);
        dba.execSQL("UPDATE Cargo SET pv = " + null);
        dba.execSQL("UPDATE Cargo SET numeroDocumento = " + null);
        dba.execSQL("UPDATE Cargo SET CantidadBultos = " + null);
        dba.execSQL("UPDATE Cargo SET isCargaVerificada = " + null);

        dba.close();
    }

    public void cargaNull(){

        SQLiteDatabase dba = dbHelper.getWritableDatabase();

        //Carga:
        dba.execSQL("UPDATE Cargo SET Carreta = " + null);
        dba.execSQL("UPDATE Cargo SET isCarga = " + null);
        dba.execSQL("UPDATE Cargo SET OrigenId = " + null);
        dba.execSQL("UPDATE Cargo SET DestinoId = " + null);
        dba.execSQL("UPDATE Cargo SET tamanoContenedor = " + null);
        dba.execSQL("UPDATE Cargo SET codigoContenedor = " + null);
        dba.execSQL("UPDATE Cargo SET numeroPrecintos = " + null);
        dba.execSQL("UPDATE Cargo SET GuiaRemision = " + null);
        dba.execSQL("UPDATE Cargo SET pv = " + null);
        dba.execSQL("UPDATE Cargo SET numeroDocumento = " + null);
        dba.execSQL("UPDATE Cargo SET CantidadBultos = " + null);
        dba.execSQL("UPDATE Cargo SET isCargaVerificada = " + null);

        dba.close();


    }
}
