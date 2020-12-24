package jiesu.fileservice.controller

import jiesu.fileservice.dto.SaveFileResponse
import jiesu.fileservice.model.FileMeta
import jiesu.fileservice.model.TextFile
import jiesu.fileservice.service.FileService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@RestController
@RequestMapping("/api/file")
class FileController(val fileService: FileService) {

    @GetMapping
    fun getFileMeta(@RequestParam path: String?): FileMeta =
        if (path == null)
            fileService.getFileInfo(".", true)
        else
            fileService.getFileInfo(decode(path), true)

    @GetMapping("/list")
    fun list(@RequestParam path: String?): List<FileMeta> =
        if (path == null)
            fileService.list(".", false)
        else
            fileService.list(decode(path), false)

    @GetMapping("/text")
    fun getTextFile(@RequestParam path: String): TextFile =
        fileService.getTextFile(decode(path))

    @PostMapping("/text")
    fun saveText(
        @RequestParam path: String,
        @RequestParam lastUpdateOn: Long,
        @RequestBody text: String
    ): SaveFileResponse {
        synchronized(this) {
            val msg = fileService.saveText(decode(path), lastUpdateOn, text)
            val meta = fileService.getFileInfo(decode(path), true)
            return SaveFileResponse(meta, msg)
        }
    }

    @GetMapping("/download")
    fun download(@RequestParam path: String): ResponseEntity<InputStreamResource> =
        fileService.download(decode(path))

    @PostMapping("/upload")
    fun upload(
        @RequestParam dir: String,
        @RequestParam file: MultipartFile,
        @RequestParam overwrite: Boolean
    ): FileMeta =
        file.inputStream.use {
            fileService.upload(
                it,
                dir,
                overwrite,
                file.originalFilename ?: throw RuntimeException("Missing file name.")
            )
        }

    fun decode(path: String): String = URLDecoder.decode(path, StandardCharsets.UTF_8.toString())
}
