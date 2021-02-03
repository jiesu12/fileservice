package jiesu.fileservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.net.URI

@Service
class SearchService(
    val fileService: FileService,
    val dir: File,
    val applicationContext: ApplicationContext,
    @Value("\${eureka.instance.metadataMap.name}") val instanceName: String,
    @Value("\${fileservice.searchservice}") val searchServiceName: String
) {

    private val supportedExtensions = listOf(
        "bat", "conf", "cs", "css", "html", "java", "js", "jsx", "json", "kt", "md",
        "py", "scss", "sh", "template", "ts", "tsx", "txt", "vim", "xml", "yml"
    )
    private val restTemplate = RestTemplate()

    fun reIndex(): Boolean {
        val searchService = getSearchService()
        restTemplate.delete("${searchService}/all?indexName=${instanceName}")
        dir.walkTopDown()
            .onEnter { it.name != ".git" }
            .filter { it.isFile }
            .forEach { index(searchService, it) }
        return true
    }

    private fun index(searchService: URI, file: File) {
        val path = file.getMeta(dir, false).fullName
        restTemplate.postForLocation(
            "${searchService}?indexName=${instanceName}&path=${path}",
            if (supportedExtensions.contains(file.extension)) file.readText() else ""
        )
    }

    private fun getSearchService(): URI {
        val discoverClient = applicationContext.getBean(DiscoveryClient::class.java)
        val searches = discoverClient.getInstances(searchServiceName)
        if (searches.isEmpty()) {
            throw RuntimeException("Search service is not available.")
        }
        return searches[0].uri
    }
}