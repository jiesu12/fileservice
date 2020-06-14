package jiesu.fileservice.controller

import jiesu.fileservice.service.SearchService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(val searchService: SearchService) {
    @PostMapping
    fun index(@RequestParam path: String) {
        searchService.index(path)
    }

    @DeleteMapping
    fun delete(@RequestParam path: String) {
        searchService.delete(path)
    }

    /**
     * Return a list of paths.
     */
    @GetMapping
    fun query(@RequestParam keyword: String): List<String> {
        return searchService.query(keyword)
    }
}