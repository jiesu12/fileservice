package jiesu.fileservice.service

import jiesu.fileservice.model.FileMeta
import jiesu.fileservice.model.TextFile
import jiesu.fileservice.model.enums.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileService(val dir: File) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun getFileInfo(path: String, detail: Boolean): FileMeta = checkPermission(path).getMeta(dir, detail)

    fun list(path: String, detail: Boolean): List<FileMeta> =
            checkPermission(path).listFiles()?.map { it.getMeta(dir, detail) }?.toList().orEmpty()

    fun getTextFile(path: String): TextFile {
        val file = checkPermission(path)
        return TextFile(file.getMeta(dir, true), file.readText())
    }

    @Synchronized
    fun saveText(path: String, lastUpdateOn: Long, text: String): String? {
        val file = checkPermission(path)
        val meta = file.getMeta(dir, true)
        var message: String? = null
        if (meta.lastUpdateOn != lastUpdateOn) {
            val backup = File(file.path + "." + System.currentTimeMillis())
            message = "File ${meta.fullName} has been modified by another program. Backing up the existing copy as ${backup} before saving."
            log.info(message)
            file.copyTo(backup, true)
        }
        file.writeText(text)
        return message
    }

    private fun checkPermission(path: String): File {
        val file = File(dir, path).normalize()
        if (!file.startsWith(dir)) {
            throw RuntimeException("Not allowed to access file - ${file.name}")
        }
        return file
    }
}

fun File.getMeta(relativeTo: File, detail: Boolean): FileMeta {
    val relativeFile = this.relativeTo(relativeTo)
    val fileInfo = FileMeta(relativeFile.path, FileType.getType(this))
    if (detail) {
        fileInfo.lastUpdateOn = this.lastModified()
        fileInfo.size = this.length()
    }
    return fileInfo
}