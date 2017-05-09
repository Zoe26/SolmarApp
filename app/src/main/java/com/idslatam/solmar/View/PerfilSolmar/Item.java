package com.idslatam.solmar.View.PerfilSolmar;

/**
 * Created by desarrollo03 on 5/2/17.
 */

import android.graphics.drawable.Drawable;

public class Item
{
    String title;
    Drawable image;

    // Empty Constructor
    public Item()
    {

    }

    // Constructor
    public Item(String title, Drawable image)
    {
        super();
        this.title = title;
        this.image = image;
    }

    // Getter and Setter Method
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Drawable getImage()
    {
        return image;
    }

    public void setImage(Drawable image)
    {
        this.image = image;
    }
}
