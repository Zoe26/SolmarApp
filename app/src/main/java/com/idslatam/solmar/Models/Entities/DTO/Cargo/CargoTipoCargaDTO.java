package com.idslatam.solmar.Models.Entities.DTO.Cargo;

public class CargoTipoCargaDTO {
    public String Nombre;
    public String ClienteCargaId;

    public CargoTipoCargaDTO(String nombre, String clienteCargaId) {
        Nombre = nombre;
        ClienteCargaId = clienteCargaId;
    }
}
