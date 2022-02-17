package xyz.junerver.sfbrowser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.chrisbanes.photoview.PhotoView
import xyz.junerver.fileselector.FileModel
import xyz.junerver.fileselector.getUriForFile
import java.io.File
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri

import xyz.junerver.fileselector.utils.FileUriUtils
import xyz.junerver.fileselector.log
import xyz.junerver.fileselector.worker.ANDROID_DATA_PATH
import java.io.FileDescriptor
import java.io.IOException


class PictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        val photoView = findViewById<PhotoView>(R.id.photo_view)
        val p = intent.getParcelableExtra<FileModel>("path")
        p.toString().log()
        p?.let {
            if (it.path.startsWith(ANDROID_DATA_PATH)) {
                it.isAndroidData = true
                it.documentFile = FileUriUtils.getDocumentFile(this,it.path)
            }
            if (!it.isAndroidData) {
                photoView.setImageURI(getUriForFile(File(it.path)))
            } else {
                it.uri?.let { uri ->
                    photoView.setImageBitmap(getBitmap(uri))
                }
            }
        }

    }

    private fun getBitmap(uri: Uri): Bitmap? {
        val fis = this.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(fis)
        fis?.close()
        return bitmap
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        "uri 转 bitmap $image".log()
        return image
    }

}