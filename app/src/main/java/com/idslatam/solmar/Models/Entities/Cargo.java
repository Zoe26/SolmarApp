package com.idslatam.solmar.Models.Entities;

/**
 * Created by desarrollo03 on 6/4/17.
 */

public class Cargo {

    public static final String TABLE_CARGO = "Cargo";

    // Labels Table Columns names
    public static final String KEY_ID_Cargo = "CargoId";
    public static final String KEY_Initial = "Initial";
    public static final String KEY_Placa = "Placa";//ok
    public static final String KEY_NombrePlaca = "NombrePlaca";
    public static final String KEY_TipoCarga = "TipoCarga";//ok
    public static final String KEY_TipoCargaForFotos = "TipoCargaForFotos";
    public static final String KEY_UpdateTipoCarga = "UpdateTipoCarga";//ok
    public static final String KEY_EppCasco = "EppCasco";//ok
    public static final String KEY_EppChaleco = "EppChaleco";//ok
    public static final String KEY_EppBotas = "EppBotas";//ok
    public static final String KEY_Dni = "Dni";//ok
    public static final String KEY_isLicencia = "isLicencia";//ok
    public static final String KEY_NroOR = "NroOR";
    public static final String KEY_isIngreso = "isIngreso";//ok
    public static final String KEY_tipoDocumento = "tipoDocumento";//iniciar
    public static final String KEY_json= "json";

    //Eliminar
    public static final String KEY_origenDestino = "origenDestino";
    public static final String KEY_fotoDelantera = "fotoDelantera";
    public static final String KEY_fotoTracera = "fotoTracera";
    public static final String KEY_fotoPanoramica = "fotoPanoramica";

    //Carga:
    public static final String KEY_Carreta = "Carreta";
    public static final String KEY_isCarga = "isCarga";
    public static final String KEY_OrigenId = "OrigenId";
    public static final String KEY_DestinoId = "DestinoId";
    public static final String KEY_tamanoContenedor = "tamanoContenedor";//iniciar//Booleano
    public static final String KEY_codigoContenedor = "codigoContenedor";
    public static final String KEY_numeroPrecintos = "numeroPrecintos";
    public static final String KEY_GuiaRemision = "GuiaRemision";//Guia Remisión o Ticket Balance
    public static final String KEY_pv = "pv";
    public static final String KEY_numeroDocumento = "numeroDocumento";
    public static final String KEY_CantidadBultos = "CantidadBultos";
    public static final String KEY_isCargaVerificada = "isCargaVerificada";

    public int CargoId;
    public String Initial;
    public String Placa;
    public String NombrePlaca;
    public String TipoCarga;
    public String TipoCargaForFotos;
    public String isLicencia;
    public String EppCasco;
    public String EppChaleco;
    public String EppBotas;
    public String tipoDocumento;
    public String Dni;
    public String isIngreso;
    public String json;

    //Carga:
    public String Carreta;
    public String isCarga;
    public String OrigenId;
    public String DestinoId;
    public String tamanoContenedor;
    public String codigoContenedor;
    public String numeroPrecintos;
    public String GuiaRemision;
    public String pv;
    public String numeroDocumento;
    public String CantidadBultos;
    public String isCargaVerificada;
    public String UpdateTipoCarga;

    public String getTipoCargaForFotos() {
        return TipoCargaForFotos;
    }

    public void setTipoCargaForFotos(String tipoCargaForFotos) {
        TipoCargaForFotos = tipoCargaForFotos;
    }

    public String getUpdateTipoCarga() {
        return UpdateTipoCarga;
    }

    public void setUpdateTipoCarga(String updateTipoCarga) {
        UpdateTipoCarga = updateTipoCarga;
    }

    public String getCarreta() {
        return Carreta;
    }

    public void setCarreta(String carreta) {
        Carreta = carreta;
    }

    public String getOrigenId() {
        return OrigenId;
    }

    public void setOrigenId(String origenId) {
        OrigenId = origenId;
    }

    public String getDestinoId() {
        return DestinoId;
    }

    public void setDestinoId(String destinoId) {
        DestinoId = destinoId;
    }

    public String getTamanoContenedor() {
        return tamanoContenedor;
    }

    public void setTamanoContenedor(String tamanoContenedor) {
        this.tamanoContenedor = tamanoContenedor;
    }

    public String getCodigoContenedor() {
        return codigoContenedor;
    }

    public void setCodigoContenedor(String codigoContenedor) {
        this.codigoContenedor = codigoContenedor;
    }

    public String getGuiaRemision() {
        return GuiaRemision;
    }

    public void setGuiaRemision(String guiaRemision) {
        GuiaRemision = guiaRemision;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getIsCargaVerificada() {
        return isCargaVerificada;
    }

    public void setIsCargaVerificada(String isCargaVerificada) {
        this.isCargaVerificada = isCargaVerificada;
    }

    public String getNumeroPrecintos() {
        return numeroPrecintos;
    }

    public void setNumeroPrecintos(String numeroPrecintos) {
        this.numeroPrecintos = numeroPrecintos;
    }

    public String getPlaca() {
        return Placa;
    }

    public void setPlaca(String placa) {
        Placa = placa;
    }

    public String getTipoCarga() {
        return TipoCarga;
    }

    public void setTipoCarga(String tipoCarga) {
        TipoCarga = tipoCarga;
    }

    public String getDni() {
        return Dni;
    }

    public void setDni(String dni) {
        Dni = dni;
    }

    public String getIsCarga() {
        return isCarga;
    }

    public void setIsCarga(String isCarga) {
        this.isCarga = isCarga;
    }

    public String getIsIngreso() {
        return isIngreso;
    }

    public void setIsIngreso(String isIngreso) {
        this.isIngreso = isIngreso;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getCantidadBultos() {
        return CantidadBultos;
    }

    public void setCantidadBultos(String cantidadBultos) {
        CantidadBultos = cantidadBultos;
    }

    public String getEppCasco() {
        return EppCasco;
    }

    public void setEppCasco(String eppCasco) {
        EppCasco = eppCasco;
    }

    public String getEppChaleco() {
        return EppChaleco;
    }

    public void setEppChaleco(String eppChaleco) {
        EppChaleco = eppChaleco;
    }

    public String getEppBotas() {
        return EppBotas;
    }

    public void setEppBotas(String eppBotas) {
        EppBotas = eppBotas;
    }

    public String getIsLicencia() {
        return isLicencia;
    }

    public void setIsLicencia(String isLicencia) {
        this.isLicencia = isLicencia;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
