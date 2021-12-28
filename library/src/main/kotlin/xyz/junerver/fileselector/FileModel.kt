package xyz.junerver.fileselector

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileModel(
    var path: String,
    var name: String,
    var extension: String,
    var size: Long,
    var date: Long,
) : Parcelable {
    @IgnoredOnParcel
    var isSelected = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileModel) return false
        return path == other.path
    }
}