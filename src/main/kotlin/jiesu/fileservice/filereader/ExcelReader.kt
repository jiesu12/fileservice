package jiesu.fileservice.filereader

import jiesu.fileservice.model.ExcelWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


@Service
class ExcelReader(
    val discoveryClient: DiscoveryClient,
    @Value("\${fileservice.excelservice}") val excelService: String
) {
    private val restTemplate = RestTemplate()

    fun read(file: File): ExcelWorkbook {
        val instances = discoveryClient.getInstances(excelService)
        if (instances.isEmpty()) {
            throw RuntimeException("Excel service is not available.")
        }
        val instance = instances[0]
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", FileSystemResource(file))
        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(body, headers)
        return restTemplate.postForObject(
            "http://" + instance.host + ":" + instance.port + "/api/read/sheets",
            requestEntity,
            ExcelWorkbook::class.java
        ) ?: ExcelWorkbook(emptyMap())
    }
}