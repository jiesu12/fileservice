package jiesu.fileservice.dto

import jiesu.fileservice.model.FileMeta

data class SaveFileResponse(val meta: FileMeta, val message: String?)