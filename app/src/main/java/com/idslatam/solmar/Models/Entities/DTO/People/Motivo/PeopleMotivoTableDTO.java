package com.idslatam.solmar.Models.Entities.DTO.People.Motivo;

import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

public class PeopleMotivoTableDTO extends BaseDTO {
    public ArrayList<PeopleMotivoItemDTO> data;

    @Override
    public String toString() {
        return "Estado:"+Estado.toString()+",Mensaje:"+Mensaje;
    }
}
