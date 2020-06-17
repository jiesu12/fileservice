package jiesu.fileservice.controller

import jiesu.fileservice.model.FileMeta
import jiesu.fileservice.model.TextFile
import jiesu.fileservice.service.FileService
import org.springframework.web.bind.annotation.*
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
    fun saveText(@RequestParam path: String, @RequestParam lastUpdateOn: Long, @RequestBody text: String): FileMeta =
            fileService.saveText(decode(path), lastUpdateOn, text)

    fun decode(path: String): String = URLDecoder.decode(path, StandardCharsets.UTF_8.toString())
}