package com.idslatam.solmar.ImageClass;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;


public class ImageConverter {

    public static Bitmap createCircleBitmap(Bitmap bitmap){// errr

        //设置一个与位图同样大小的新位图
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        //设置一个图片大小的矩形
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        int halfWidth = bitmap.getWidth()/2;
        int halfHeight = bitmap.getHeight()/2;
        canvas.drawCircle(halfWidth, halfHeight, Math.max(halfWidth, halfHeight), paint);
        //设置为取两层图像交集部门,只显示上层图像
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //画图像
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /*public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }*/
}
