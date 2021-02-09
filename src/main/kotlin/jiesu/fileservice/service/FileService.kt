package jiesu.fileservice.service

import jiesu.fileservice.filereader.ExcelReader
import jiesu.fileservice.model.ExcelFile
import jiesu.fileservice.model.FileMeta
import jiesu.fileservice.model.TextFile
import jiesu.fileservice.model.enums.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.File
import java.io.InputStream
import java.io.OutputStream


@Service
class FileService(val dir: File, val excelReader: ExcelReader) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(FileService::class.java)
    }

    fun getFileInfo(path: String, detail: Boolean): FileMeta = checkPermission(path).getMeta(dir, detail)

    fun list(path: String): List<FileMeta> =
        checkPermission(path).listFiles()?.map { it.getMeta(dir, true) }?.toList().orEmpty()

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
        respHeaders.contentDisposition = ContentDisposition.builder("inline").filename(file.name).build()
        respHeaders.lastModified = file.lastModified()

        val inputStream: InputStream = file.inputStream()
        respHeaders.add("Content-Length", (file.getMeta(dir, true).size ?: 0).toString())
        val isr = InputStreamResource(inputStream)
        return ResponseEntity(isr, respHeaders, HttpStatus.OK)
    }

    /**
     * The 'Accept-Ranges: bytes' code has been removed. It had problem playing longer video on Chrome.
     */
    fun stream(path: String, contentType: String?, range: String?): ResponseEntity<StreamingResponseBody> {
        val file = checkPermission(path)
        val respHeaders = HttpHeaders()
        respHeaders.contentType = file.getMediaType()
        respHeaders.add("Accept-Ranges", "none")
        respHeaders.lastModified = file.lastModified()
        val fileIs: InputStream = file.inputStream()
        val fileLength = file.length()
        respHeaders.contentLength = fileLength
        return ResponseEntity(StreamingResponseBody { pipe(fileIs, it) }, respHeaders, HttpStatus.OK)
    }

    fun upload(
        inputStream: InputStream,
        dirName: String,
        overwrite: Boolean,
        filename: String,
        lastModified: Long?
    ): FileMeta {
        val parent = checkPermission(dirName)
        if (parent.isFile) {
            throw RuntimeException("File upload destination is not a directory.")
        }
        val target: File = parent.resolve(filename)
        if (!overwrite && target.exists()) {
            throw RuntimeException("$filename already exists.")
        }
        target.outputStream().use { inputStream.copyTo(it) }
        if (lastModified != null) {
            target.setLastModified(lastModified)
        }
        return target.getMeta(dir, true)
    }

    fun rename(path: String, newName: String): FileMeta {
        val file = checkPermission(path)
        val newFile = file.parentFile.resolve(newName)
        if (file.renameTo(newFile)) {
            return newFile.getMeta(dir, true)
        } else {
            throw RuntimeException("Failed to rename.")
        }
    }

    fun create(path: String, folder: Boolean): FileMeta {
        val file = checkPermission(path)
        if (file.exists()) {
            throw RuntimeException("File already exist.")
        }
        if (folder) {
            file.mkdirs()
        } else {
            file.createNewFile()
        }
        return file.getMeta(dir, true)
    }

    fun getExcelFile(path: String): ExcelFile {
        val file = checkPermission(path)
        return ExcelFile(file.getMeta(dir, true), excelReader.read(file))
    }

    /**
     * Return deleted file's parent.
     */
    fun delete(path: String): FileMeta {
        val recycleBin: File = dir.resolve("recycle")
        recycleBin.mkdirs()

        val file = checkPermission(path)
        if (!file.exists()) {
            throw RuntimeException("File doesn't exist.")
        } else if (file == dir || file == recycleBin) {
            throw RuntimeException("Can't delete this directory.")
        } else if (file.startsWith(recycleBin)) {
            if (!file.delete()) {
                throw RuntimeException("Failed to permanently delete file, make sure directory is empty.")
            }
        } else {
            if (!file.renameTo(recycleBin.resolve("${file.name}.${System.currentTimeMillis()}"))) {
                throw RuntimeException("Failed to recycle file.")
            }
        }
        return file.parentFile.getMeta(dir, false)
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

private fun pipe(inputStream: InputStream, outputStream: OutputStream) {
    val data = ByteArray(2048)
    var read: Int
    while (inputStream.read(data).also { read = it } > 0) {
        outputStream.write(data, 0, read)
    }
    outputStream.flush()
}