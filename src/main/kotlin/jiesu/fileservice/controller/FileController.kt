package jiesu.fileservice.controller

import jiesu.fileservice.dto.BooleanResponse
import jiesu.fileservice.model.FileInfo
import jiesu.fileservice.service.FileService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/file")
class FileController(val fileService: FileService) {

    @GetMapping
    fun getFile(@RequestParam path: String?): FileInfo =
            if (path == null)
                fileService.getFileInfo(".", true)
            else
                fileService.getFileInfo(path, true)

    @GetMapping("/list")
    fun list(@RequestParam path: String?): List<FileInfo> =
            if (path == null)
                fileService.list(".", false)
            else
                fileService.list(path, false)

    @GetMapping("/text")
    fun getText(@RequestParam path: String) =
            fileService.getText(path)

    @PostMapping("/checkout")
    fun checkout(@RequestParam path: String) =
            BooleanResponse(fileService.checkout(path))

    @PostMapping("/checkin")
    fun checkIn(@RequestParam path: String) =
            BooleanResponse(fileService.checkIn(path))

    @PostMapping("/text")
    fun saveText(@RequestParam path: String, @RequestBody text: String) = fileService.saveText(path, text)
}