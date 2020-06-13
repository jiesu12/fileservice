package jiesu.fileservice.service

import jiesu.fileservice.model.FileInfo
import jiesu.fileservice.model.enums.FileType
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileService(val dir: File) {

    private val checkouts: MutableSet<String> = mutableSetOf()

    fun getFileInfo(path: String, detail: Boolean): FileInfo = checkPermission(path).fileInfo(dir, detail)

    fun list(path: String, detail: Boolean): List<FileInfo> =
            checkPermission(path).listFiles()?.map { it.fileInfo(dir, true) }?.toList().orEmpty()

    fun getText(path: String): String = checkPermission(path).readText()

    fun checkout(path: String): Boolean {
        val jPath = checkPermission(path).fileInfo(dir, false).fullName
        return if (checkouts.contains(jPath)) {
            false
        } else {
            checkouts.add(jPath)
            true
        }
    }

    fun checkIn(path: String): Boolean =
            checkouts.remove(checkPermission(path).fileInfo(dir, false).fullName)

    fun saveText(path: String, text: String) {
        checkPermission(path).writeText(text)
    }

    private fun checkPermission(path: String): File {
        val file = File(dir, path).normalize()
        if (!file.startsWith(dir)) {
            throw RuntimeException("Not allowed to access file - ${file.name}")
        }
        return file
    }
}

fun File.fileInfo(relativeTo: File, detail: Boolean): FileInfo {
    val relativeFile = this.relativeTo(relativeTo)
    val fileInfo = FileInfo(relativeFile.path, FileType.getType(this))
    if (detail) {
        fileInfo.lastUpdateOn = this.lastModified()
        fileInfo.size = this.length()
    }
    return fileInfo
}