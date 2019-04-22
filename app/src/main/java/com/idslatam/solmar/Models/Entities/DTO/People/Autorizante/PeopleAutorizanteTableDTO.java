package com.idslatam.solmar.Models.Entities.DTO.People.Autorizante;

import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

public class PeopleAutorizanteTableDTO extends BaseDTO {
    public ArrayList<PeopleAutorizanteItemDTO> data;

    @Override
    public String toString() {
        return "Estado:"+Estado.toString()+",Mensaje:"+Mensaje;
    }
}
