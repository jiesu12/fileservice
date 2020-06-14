package jiesu.fileservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@TypeAlias("file") // change "_class" attribute from "jiesu.fileservice.model.SearchableFile" to "file".
data class SearchableFile(@Id @Field val path: String, @Field(type = FieldType.Text) val content: String?)