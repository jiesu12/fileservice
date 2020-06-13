package jiesu.fileservice.service

import jiesu.fileservice.model.SearchableFile
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Service
import java.io.File


@Service
class SearchService(val elasticsearchOperations: ElasticsearchOperations,
                    val fileService: FileService,
                    val dir: File) {

    fun index(path: String) {
        val fileInfo = fileService.getFileInfo(path, false)
        val file = File(dir, fileInfo.fullName)
        val searchableFile = SearchableFile(fileInfo.fullName, file.readText())
        elasticsearchOperations.save(searchableFile)
    }

    fun query(query: String): SearchableFile? {
        return elasticsearchOperations.get(query, SearchableFile::class.java)
    }
}