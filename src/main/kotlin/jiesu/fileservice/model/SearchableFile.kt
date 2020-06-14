package jiesu.fileservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "file")
@TypeAlias("file") // change "_class" attribute from "jiesu.fileservice.model.SearchableFile" to "file".
data class SearchableFile(@Id val path: String, @Field(type = FieldType.Text) val content: String?)