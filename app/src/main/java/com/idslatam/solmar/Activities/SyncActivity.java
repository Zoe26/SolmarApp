package com.idslatam.solmar.Activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.idslatam.solmar.Models.Crud.CargoFormFotoCrud;
import com.idslatam.solmar.Models.Entities.CargoFormFoto;
import com.idslatam.solmar.R;

import org.w3c.dom.Text;

import java.util.List;

public class SyncActivity extends AppCompatActivity {

    Context mContext = null;
    TextView txtData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mContext = this;
        txtData = (TextView)findViewById(R.id.txtData);
        String info = "";

        txtData.setText(info);

        CargoFormFotoCrud _imagenCargo = new CargoFormFotoCrud(mContext);
        List<CargoFormFoto> cargoFotos = _imagenCargo.listFotosForSync();
        for (CargoFormFoto cargoFoto: cargoFotos) {
            info =   info
                    +"Id:    "+cargoFoto.getCargoFormFotoId()+"\n"
                    +"Cod Sync:    "+cargoFoto.codigoSincronizacion+"\n"
                    +"Indice:      "+cargoFoto.indice+"\n"
                    +"FilePath:    "+cargoFoto.filePath+"\n"
                    +"CliCarFotId: "+(cargoFoto.clienteCargaFotoId!=null?cargoFoto.clienteCargaFotoId:"Precinto")+"\n"
                    +"============================="+"\n";
            ;
        }
        txtData.setText(info);

    }
}
