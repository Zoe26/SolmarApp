package com.idslatam.solmar.Cargo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by anupamchugh on 26/12/15.
 */
public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;

    public CustomPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {


        Log.e("Item Position",String.valueOf(position));

        ModelObject modelObjectX6 = ModelObject.values()[0];
        Log.e("Item Adapter x0",String.valueOf(modelObjectX6.getTitleResId()));

        ModelObject modelObjectX = ModelObject.values()[1];
        Log.e("Item Adapter x1",String.valueOf(modelObjectX.getTitleResId()));

        ModelObject modelObjectX5 = ModelObject.values()[2];
        Log.e("Item Adapter x2",String.valueOf(modelObjectX5.getTitleResId()));



        ModelObject modelObject = ModelObject.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(modelObject.getLayoutResId(), collection, false);

        collection.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return ModelObject.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        ModelObject customPagerEnum = ModelObject.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }

}
