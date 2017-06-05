package com.idslatam.solmar.Cargo;

import com.idslatam.solmar.R;

/**
 * Created by anupamchugh on 26/12/15.
 */
public enum ModelObject {

    RED(R.string.red, R.layout.view_primero),
    BLUE(R.string.blue, R.layout.view_segundo),
    GREEN(R.string.green, R.layout.view_tercero);

    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}
