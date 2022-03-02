package xyz.junerver.fileselector

import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import xyz.junerver.fileselector.utils.FileUriUtils
import java.io.File

@Parcelize
data class FileModel(
    var path: String,
    var name: String,
    var extension: String,
    var size: Long,
    var date: Long,
) : Parcelable {
    //是不是data文件
    @IgnoredOnParcel
    var isAndroidData = false

    @IgnoredOnParcel
    var uri: Uri? = null

    @IgnoredOnParcel
    var documentFile: DocumentFile? = null
        set(value) {
            field = value
            this.uri = value?.uri
            this.isAndroidData = true
        }

    @IgnoredOnParcel
    var isSelected = false

    @IgnoredOnParcel
    var similarity: Double = 0.0

    //根据文件修改模型
    fun update(f: File) {
        this.path = f.absolutePath
        this.name = f.name
        this.date = f.lastModified()
    }

    fun update(doc: DocumentFile) {
        this.uri = doc.uri
        this.path = FileUriUtils.treeToPath(doc.uri.toString()).decodeURL()
        this.name = doc.name.toString()
        this.date = doc.lastModified()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileModel) return false
        return path == other.path
    }
}