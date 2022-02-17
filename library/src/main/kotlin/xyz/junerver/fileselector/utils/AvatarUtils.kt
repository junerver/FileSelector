package xyz.junerver.fileselector.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 生成头像使用工具类
 *
 * @author Lee
 */
object AvatarUtils {
    fun generateDefaultAvatar(context: Context, text: String): String {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 220f
        paint.isAntiAlias = true
        val width = 480
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#82b2ff"))
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        val fm = paint.fontMetrics
        val textLeft = ((width - paint.measureText(text)) / 2).toInt()
        val textTop = (height - width / 2 + Math.abs(fm.ascent) / 2 - 25).toInt()
        canvas.drawText(text, textLeft.toFloat(), textTop.toFloat(), paint)
        return saveBitmap(context, bitmap, "$text.jpg")
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, name: String): String {
        val file = File(context.cacheDir.toString() + "/" + name)
        if (file.exists()) {
            return file.absolutePath
        }
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }
}