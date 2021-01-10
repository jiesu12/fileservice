package jiesu.fileservice.model

data class ExcelWorkbook(val sheets: Map<String, List<List<String>>>)
