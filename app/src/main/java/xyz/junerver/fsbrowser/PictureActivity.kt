package xyz.junerver.fsbrowser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.chrisbanes.photoview.PhotoView
import xyz.junerver.fileselector.getUriForFile
import java.io.File

class PictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        val photoView = findViewById<PhotoView>(R.id.photo_view)
        val p = intent.getStringExtra("path")
        photoView.setImageURI(getUriForFile(File(p)))
    }
}