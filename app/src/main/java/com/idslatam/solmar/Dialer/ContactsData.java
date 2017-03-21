package com.idslatam.solmar.Dialer;

/**
 * Created by desarrollo03 on 3/19/17.
 */

public class ContactsData {

    public int ContactsId;
    public String Nombre;
    public String NumeroP;
    public String NumeroS;

    public ContactsData(int contactsId, String nombre, String numeroP,String numeroS) {
        this.NumeroS = numeroS;
        this.ContactsId = contactsId;
        this.Nombre = nombre;
        this.NumeroP = numeroP;
    }

    /*public ContactsData( int contactsId, String nombre) {
        this.ContactsId = contactsId;
        this.Nombre = nombre;
    }*/

    public int getContactsId() {
        return ContactsId;
    }

    public void setContactsId(int contactsId) {
        ContactsId = contactsId;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getNumeroP() {
        return NumeroP;
    }

    public void setNumeroP(String numeroP) {
        NumeroP = numeroP;
    }

    public String getNumeroS() {
        return NumeroS;
    }

    public void setNumeroS(String numeroS) {
        NumeroS = numeroS;
    }

}
