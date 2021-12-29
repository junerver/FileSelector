package xyz.junerver.fileselector

import java.util.Comparator

/**
 * Description:
 * @author Junerver
 * date: 2021/12/27-10:26
 * Email: junerver@gmail.com
 * Version: v1.0
 */
internal class SortByExtension : Comparator<FileModel?> {
    override fun compare(f1: FileModel?, f2: FileModel?): Int {
        return if (f1 == null || f2 == null) {
            if (f1 == null) {
                -1
            } else {
                1
            }
        } else {
            val ext_1 = f1.extension
            val ext_2 = f2.extension
            if (ext_1.equals(ext_2, ignoreCase = true)) {
                SortByName().compare(f1, f2)
            } else {
                ext_1.compareTo(ext_2, ignoreCase = true)
            }
        }
    }
}

internal class SortByName : Comparator<FileModel?> {
    override fun compare(f1: FileModel?, f2: FileModel?): Int {
        return if (f1 == null || f2 == null) {
            if (f1 == null) {
                -1
            } else {
                1
            }
        } else {
            f1.name.compareTo(f2.name, ignoreCase = true)
        }
    }
}

internal class SortBySize : Comparator<FileModel?> {
    override fun compare(f1: FileModel?, f2: FileModel?): Int {
        return if (f1 == null || f2 == null) {
            if (f1 == null) {
                -1
            } else {
                1
            }
        } else {
            when {
                f1.size > f2.size -> 1
                f1.size < f2.size -> -1
                else -> 0
            }
        }
    }
}

internal class SortByTime : Comparator<FileModel?> {
    override fun compare(f1: FileModel?, f2: FileModel?): Int {
        return if (f1 == null || f2 == null) {
            if (f1 == null) {
                -1
            } else {
                1
            }
        } else {
            when {
                f1.date > f2.date -> 1
                f1.date < f2.date -> -1
                else -> 0
            }
        }
    }
}