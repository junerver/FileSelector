package com.lee.fileselector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 生成头像使用工具类
 *
 * @author Lee
 */
public class AvatarUtils {

    public static String generateDefaultAvatar(Context context, String text) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(220);
        paint.setAntiAlias(true);
        int width = 480;
        int height = 480;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#82b2ff"));
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        Paint.FontMetrics fm = paint.getFontMetrics();
        int textLeft = (int) ((width - paint.measureText(text)) / 2);
        int textTop = (int) (height - width / 2 + Math.abs(fm.ascent) / 2 - 25);
        canvas.drawText(text, textLeft, textTop, paint);
        return saveBitmap(context, bitmap, text + ".jpg");
    }

    private static String saveBitmap(Context context, Bitmap bitmap, String name) {
        File file = new File(context.getCacheDir() + "/" + name);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}
