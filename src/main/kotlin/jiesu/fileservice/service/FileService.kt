package jiesu.fileservice.service

import jiesu.fileservice.model.FileMeta
import jiesu.fileservice.model.TextFile
import jiesu.fileservice.model.enums.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream


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
            message =
                "File ${meta.fullName} has been modified by another program. Backing up the existing copy as $backup before saving."
            log.info(message)
            file.copyTo(backup, true)
        }
        file.writeText(text)
        return message
    }

    fun download(path: String): ResponseEntity<InputStreamResource> {
        val file = checkPermission(path)
        val respHeaders = HttpHeaders()
        respHeaders.contentType = file.getMediaType()
        respHeaders.contentDisposition = ContentDisposition.builder("inline").filename(path).build()

        val inputStream: InputStream = file.inputStream()
        respHeaders.add("Content-Length", (file.getMeta(dir, true).size ?: 0).toString())
        val isr = InputStreamResource(inputStream)
        return ResponseEntity(isr, respHeaders, HttpStatus.OK)
    }

    fun upload(inputStream: InputStream, dirName: String, overwrite: Boolean, filename: String): FileMeta {
        val parent = checkPermission(dirName)
        if (parent.isFile) {
            throw RuntimeException("File upload destination is not a directory.")
        }
        val target: File = parent.resolve(filename)
        if (!overwrite && target.exists()) {
            throw RuntimeException("$filename already exists.")
        }
        target.outputStream().use { inputStream.copyTo(it) }
        return target.getMeta(dir, true)
    }

    fun rename(path: String, newName: String): FileMeta {
        val file = checkPermission(path)
        val newFile = file.parentFile.resolve(newName)
        if (file.renameTo(newFile)) {
            return newFile.getMeta(dir, false)
        } else {
            throw RuntimeException("Failed to rename.")
        }
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

fun File.getMediaType(): MediaType {
    return when (extension.toLowerCase()) {
        "pdf" -> {
            MediaType.APPLICATION_PDF
        }
        "txt", "html" -> {
            MediaType.TEXT_HTML
        }
        "json" -> {
            MediaType.APPLICATION_JSON
        }
        "xml" -> {
            MediaType.APPLICATION_XML
        }
        else -> {
            MediaType.APPLICATION_OCTET_STREAM
        }
    }
}