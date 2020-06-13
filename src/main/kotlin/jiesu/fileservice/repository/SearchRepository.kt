package jiesu.fileservice.repository

import jiesu.fileservice.model.SearchableFile
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Service

@NoRepositoryBean
@Service
interface SearchRepository : Repository<SearchableFile, String> {
    fun findById(id: String): SearchableFile?
}