package com.idslatam.solmar.Models.Entities;

public class CargoTipoFoto {

    public static final String TABLE_NAME = "CargoTipoFoto";
    public static final String KEY_ID = "CargoTipoFotoId";
    public static final String KEY_NOMBRE = "Nombre";
    public static final String KEY_CLIENTE_CARGA_FOTO_ID = "ClienteCargaFotoId";
    public static final String KEY_FILE_PATH = "FilePath";

    public long CargoTipoFotoId;
    public String Nombre;
    public String ClienteCargaFotoId;
    public String FilePath;

    public CargoTipoFoto(String nombre, String clienteCargaFotoId) {

        Nombre = nombre;
        ClienteCargaFotoId = clienteCargaFotoId;
    }

    public long getCargoTipoFotoId() {
        return CargoTipoFotoId;
    }

    public void setCargoTipoFotoId(long cargoTipoFotoId) {
        CargoTipoFotoId = cargoTipoFotoId;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getClienteCargaFotoId() {
        return ClienteCargaFotoId;
    }

    public void setClienteCargaFotoId(String clienteCargaFotoId) {
        ClienteCargaFotoId = clienteCargaFotoId;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }
}
