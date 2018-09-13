package com.idslatam.solmar.Models.Entities.DTO.Cargo;

import com.idslatam.solmar.Models.Entities.DTO.BaseDTO;

import java.util.ArrayList;

public class CargoFotoFormDTO extends BaseDTO {
    public ArrayList<CargoFotoFormDataDTO>  Data;
    public String ClienteCarga;
    public String ClienteCargaId;
    public int Total;
}
