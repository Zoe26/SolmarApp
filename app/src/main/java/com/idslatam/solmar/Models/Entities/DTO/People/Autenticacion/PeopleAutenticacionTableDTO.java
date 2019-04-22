package com.idslatam.solmar.Models.Entities.DTO.People.Autenticacion;

import com.google.gson.annotations.Expose;
import com.idslatam.solmar.ImageClass.MessageView;
import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class PeopleAutenticacionTableDTO extends BaseDTO
{
    public ArrayList<PeopleAutenticacionItemDTO> Data;

    @Override
    public String toString() {
        return "Estado:"+Estado.toString()+",Mensaje:"+Mensaje;
    }
}
