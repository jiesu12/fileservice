package jiesu.fileservice.controller

import jiesu.fileservice.dto.BooleanResponse
import jiesu.fileservice.service.SearchService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchController(val searchService: SearchService) {
    @PostMapping("/reindex")
    fun reIndex(): BooleanResponse {
        return BooleanResponse(searchService.reIndex())
    }
}