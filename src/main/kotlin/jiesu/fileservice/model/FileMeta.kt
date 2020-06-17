package jiesu.fileservice.model

import jiesu.fileservice.model.enums.FileType

/**
 * fullName is the relative path plus name. Relative path is the path relative to the file base dir.
 */
data class FileMeta(val fullName: String,
                    val type: FileType,
                    var size: Long? = null,
                    var lastUpdateOn: Long? = null)