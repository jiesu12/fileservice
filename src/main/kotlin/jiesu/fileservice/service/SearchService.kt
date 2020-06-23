package jiesu.fileservice.service

import jiesu.fileservice.model.SearchableFile
import jiesu.fileservice.spring.TokenAuthenticationFilter
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import java.io.File

@Service
class SearchService(val elasticsearchOperations: ElasticsearchOperations,
                    val fileService: FileService,
                    val dir: File,
                    val indexCoordinates: IndexCoordinates) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(SearchService::class.java)
    }

    fun index(path: String) {
        val fileInfo = fileService.getFileInfo(path, false)
        val file = File(dir, fileInfo.fullName)
        val searchableFile = SearchableFile(fileInfo.fullName, file.readText())
        elasticsearchOperations.save(searchableFile, indexCoordinates)
    }

    fun query(keyword: String): List<String> {
        val pathQuery = NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("path", keyword)).build()
        val pathHits = elasticsearchOperations.search(pathQuery, SearchableFile::class.java, indexCoordinates)
        val contentQuery = NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("content", keyword)).build()
        val contentHits = elasticsearchOperations.search(contentQuery, SearchableFile::class.java, indexCoordinates)
        return (pathHits.searchHits + contentHits.searchHits).mapNotNull { it.content.path }
    }

    fun delete(path: String) {
        elasticsearchOperations.delete(path, indexCoordinates)
    }

    fun reIndex(): Boolean {
        val query = NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build()
        try {
            elasticsearchOperations.delete(query, SearchableFile::class.java, indexCoordinates)
        } catch (e: Exception) {
            log.info("First time indexing.")
        }
        dir.walkTopDown().onEnter { it.name != ".git" }.filter { it.isFile }.forEach { index(it.getMeta(dir, false).fullName) }
        return true
    }
}