package com.idslatam.solmar.Cargo.Foto;

import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoFotoFormDataDTO;

public class CargoFotoDataModel extends CargoFotoFormDataDTO {
    public String foto;

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
