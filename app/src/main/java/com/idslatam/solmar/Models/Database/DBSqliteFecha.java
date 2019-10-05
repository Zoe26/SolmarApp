package com.idslatam.solmar.Models.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;

import com.idslatam.solmar.View.MostrarFecha;

public class DBSqliteFecha extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME="BDFecha";
    public static final String TABLE_TIEMPO="ID";
    public static final String KEY_ID = "id" ;
    public static final String KEY_FECHA = "fecha";
    public static final String KEY_TIEMPO = "tiempo";

    public DBSqliteFecha(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

   @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TIEMPO_TABLE = "CREATE TABLE"+TABLE_TIEMPO+"("
                +KEY_ID+"INTEGER PRIMARY KEY,"
                +KEY_FECHA+"TEXT,"
                +KEY_TIEMPO+"TEXT"+")";

        db.execSQL(CREATE_TIEMPO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS" + TABLE_TIEMPO);
        onCreate(db);

    }

   /* void addTiempo(MostrarFecha mfecha){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FECHA, mfecha.getColor());
        values.put(KEY_TIEMPO, mfecha.getTiempo());

        db.insert(TABLE_TIEMPO, null, values);
        db.close();
    }

/*

    MostrarFecha getTiempo(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TIEMPO, new String[]{KEY_ID, KEY_FECHA, KEY_TIEMPO}, KEY_ID+"=?",new String[]{String.valueOf(id)}, null,null,null,null);
        if(cursor !=null){
            cursor.moveToFirst();
        }
        MostrarFecha fecha = new MostrarFecha(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));

        return fecha;
    }
    */
/*
    public List<MostrarFecha>getAllTiempo(){
        List<MostrarFecha> tiempoList = new ArrayList<>();

        String selectQuery ="SELECT * FROM" + TABLE_TIEMPO;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                MostrarFecha fecha = new MostrarFecha();
                fecha.setId(Integer.parseInt(cursor.getString(0)));
                fecha.setFecha(cursor.get);

            }while (cursor.moveToNext());
        }

        return tiempoList;
    }
    */
/*
    public int updateTiempo(MostrarFecha fecha){
        SQLiteDatabase db = this.getWritableDatabase();

        MostrarFechaValues values=new MostrarFechaValues();
        values.put(KEY_FECHA, fecha.getFecha());
        values.put(KEY_TIEMPO, fecha.getFecha());

        return db.update(TABLE_TIEMPO, values, KEY_ID+ "=?", new String[]{String.valueOf(fecha.getId())});
    }

    public void deleteTiempo(MostrarFecha fecha){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_TIEMPO, KEY_ID + "=)", new String[]{String.valueOf(fecha.getId())});
        db.close();
    }
*/
   /* public int getTiempoCount(){
        String countQuery = "SELECT * FROM" + TABLE_TIEMPO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery,null);
        cursor.close();

        return cursor.getCount();
    }*/
}
