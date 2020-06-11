package jiesu.fileservice.service

import jiesu.fileservice.model.JFile
import jiesu.fileservice.model.enums.FileType
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileService(val dir: File) {

    private val checkouts: MutableSet<String> = mutableSetOf()

    fun getJFile(path: String, detail: Boolean): JFile = checkPermission(path).toJFile(dir, detail)

    fun list(path: String, detail: Boolean): List<JFile> =
            checkPermission(path).listFiles()?.map { it.toJFile(dir, true) }?.toList().orEmpty()

    fun getText(path: String): String = checkPermission(path).readText()

    fun checkout(path: String): Boolean {
        val jPath = checkPermission(path).toJFile(dir, false).fullName
        return if (checkouts.contains(jPath)) {
            false
        } else {
            checkouts.add(jPath)
            true
        }
    }

    fun checkIn(path: String): Boolean =
            checkouts.remove(checkPermission(path).toJFile(dir, false).fullName)

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

fun File.toJFile(relativeTo: File, detail: Boolean): JFile {
    val relativeFile = this.relativeTo(relativeTo)
    val jfile = JFile(relativeFile.path, FileType.getType(this))
    if (detail) {
        jfile.lastUpdateOn = this.lastModified()
        jfile.size = this.length()
    }
    return jfile
}