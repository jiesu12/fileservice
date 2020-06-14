package jiesu.fileservice.service

import jiesu.fileservice.model.SearchableFile
import org.elasticsearch.index.query.QueryBuilders
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
}