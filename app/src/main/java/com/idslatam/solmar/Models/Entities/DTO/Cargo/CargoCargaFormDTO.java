package com.idslatam.solmar.Models.Entities.DTO.Cargo;

import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

public class CargoCargaFormDTO extends BaseDTO{

    public String ClienteCarga;
    public ArrayList<CargoCargaFormDataDTO> Data;
    public int Total;

}
