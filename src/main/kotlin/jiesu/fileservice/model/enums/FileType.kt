package jiesu.fileservice.model.enums

import java.io.File

enum class FileType {
    REGULAR,
    DIR;

    companion object {
        fun getType(file: File): FileType =
                if (file.isFile) {
                    REGULAR
                } else {
                    DIR
                }
    }
}