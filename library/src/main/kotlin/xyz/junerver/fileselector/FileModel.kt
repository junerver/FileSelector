package xyz.junerver.fileselector

import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileModel(
    var path: String,
    var name: String,
    var extension: String,
    var size: Long,
    var date: Long,
)  {
    //是不是data文件
    var isAndroidData = false
    var documentFile: DocumentFile? = null
    var isSelected = false
    var similarity:Double = 0.0

    fun update(f: File) {
        val pathStr = f.absolutePath
        this.path = f.absolutePath
        this.name = f.name
        this.date = f.lastModified()
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileModel) return false
        return path == other.path
    }
}