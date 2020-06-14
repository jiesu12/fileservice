package jiesu.fileservice.controller

import jiesu.fileservice.dto.BooleanResponse
import jiesu.fileservice.model.FileInfo
import jiesu.fileservice.service.FileService
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/file")
class FileController(val fileService: FileService) {

    @GetMapping
    fun getFile(@RequestParam path: String?): FileInfo =
            if (path == null)
                fileService.getFileInfo(".", true)
            else
                fileService.getFileInfo(decode(path), true)

    @GetMapping("/list")
    fun list(@RequestParam path: String?): List<FileInfo> =
            if (path == null)
                fileService.list(".", false)
            else
                fileService.list(decode(path), false)

    @GetMapping("/text")
    fun getText(@RequestParam path: String) =
            fileService.getText(decode(path))

    @PostMapping("/checkout")
    fun checkout(@RequestParam path: String) =
            BooleanResponse(fileService.checkout(decode(path)))

    @PostMapping("/checkin")
    fun checkIn(@RequestParam path: String) =
            BooleanResponse(fileService.checkIn(decode(path)))

    @PostMapping("/text")
    fun saveText(@RequestParam path: String, @RequestBody text: String) = fileService.saveText(decode(path), text)

    fun decode(path: String): String = URLDecoder.decode(path, StandardCharsets.UTF_8.toString())
}