package com.idslatam.solmar.Cargo.TipoCarga;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RadioButton;

import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoTipoCargaDTO;
import com.idslatam.solmar.R;

import java.util.ArrayList;

public class TipoCargaCustomPagerAdapter extends ArrayAdapter<CargoTipoCargaDTO> {
    private Context mContext;
    ArrayList<CargoTipoCargaDTO> tipoCarga;
    private int selectedPosition = -1;
    CargoCrud cargoCrud;

    public TipoCargaCustomPagerAdapter(Context context, ArrayList<CargoTipoCargaDTO> tipoCarga) {

        super(context, 0, tipoCarga);
        this.mContext = context;
        this.tipoCarga = tipoCarga;
        this.cargoCrud = new CargoCrud(context);

    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public CargoTipoCargaDTO getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable CargoTipoCargaDTO item) {
        return super.getPosition(item);
    }

    public CargoTipoCargaDTO getItemX() {

        //Log.e("selectedPosition",String.valueOf(selectedPosition));

        if(selectedPosition == -1){
            CargoTipoCargaDTO objReturn = new CargoTipoCargaDTO("","");
            return objReturn;
        }
        return super.getItem(selectedPosition);
    }

    public void removeSelection(){

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int positionx = position;
        //ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cargo_tipo_carga_adapter_layout, parent, false);


            //rbtnTipoCarga.setChecked();

            //viewHolder.label

            /*

            final CargoTipoCargaDTO objSelect =  cargaTipo;


            RadioButton rbtnTipoCarga = (RadioButton)convertView.findViewById(R.id.rbtnTipoCarga);

            rbtnTipoCarga.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Log.e("Checked ",String.valueOf(isChecked));

                    if(isChecked){
                        cargaTipo.put
                    }

                    Log.e("Id ",objSelect.ClienteCargaId);
                    Log.e("Nombre ",objSelect.Nombre);
                }
            });
            */


        }


        CargoTipoCargaDTO cargaTipo = getItem(position);

        RadioButton rbtnTipoCarga = (RadioButton)convertView.findViewById(R.id.rbtnTipoCarga);
        rbtnTipoCarga.setText(cargaTipo.Nombre);

        Cargo cargo = cargoCrud.getCargo();

        if(cargo.getTipoCarga() != null && !cargo.getTipoCarga().isEmpty()){
            if(cargo.getTipoCarga().equalsIgnoreCase(cargaTipo.ClienteCargaId)){
                rbtnTipoCarga.setChecked(true);
                cargoCrud.updateFieldGeneric("UpdateTipoCarga","true",1);
                selectedPosition = position;
            }else {
                rbtnTipoCarga.setChecked(position == selectedPosition);
            }
        }
        else{
            rbtnTipoCarga.setChecked(position == selectedPosition);
        }



        rbtnTipoCarga.setTag(position);

        rbtnTipoCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedPosition = positionx;

                String Id =  tipoCarga.get(selectedPosition).ClienteCargaId;
                String Nombre =  tipoCarga.get(selectedPosition).Nombre;
                //Log.e("Id",Id);
                //Log.e("Nombre",Nombre);

                cargoCrud = new CargoCrud(mContext);
                cargoCrud.updateTipoCarga(Id,1);
                cargoCrud.updateFieldGeneric("UpdateTipoCarga","true",1);

                notifyDataSetChanged();
            }
        });


        //convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT,20));
        //return super.getView(position, convertView, parent);
        return  convertView;
    }


}
