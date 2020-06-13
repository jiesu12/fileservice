package jiesu.fileservice.controller

import jiesu.fileservice.model.SearchableFile
import jiesu.fileservice.service.SearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchController(val searchService: SearchService) {
    @GetMapping("/index")
    fun test() {
        searchService.index("test.txt")
    }

    @GetMapping("/query")
    fun query(): SearchableFile? {
        return searchService.query("test.txt")
    }
}