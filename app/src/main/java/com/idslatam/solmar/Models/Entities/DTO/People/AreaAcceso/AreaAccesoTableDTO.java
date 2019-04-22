package com.idslatam.solmar.Models.Entities.DTO.People.AreaAcceso;

import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

public class AreaAccesoTableDTO extends BaseDTO {
    public ArrayList<AreaAccesoItemDTO> data;

    @Override
    public String toString() {
        return "Estado:"+Estado.toString()+",Mensaje:"+Mensaje;
    }
}
