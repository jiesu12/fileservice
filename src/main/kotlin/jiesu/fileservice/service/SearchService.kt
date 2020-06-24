package jiesu.fileservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.net.URI

@Service
class SearchService(val fileService: FileService,
                    val dir: File,
                    val applicationContext: ApplicationContext,
                    @Value("\${eureka.instance.metadataMap.name}") val instanceName: String) {

    private val supportedExtensions = listOf("txt", "md", "sh")
    private val restTemplate = RestTemplate()

    fun reIndex(): Boolean {
        val searchService = getSearchService()
        restTemplate.delete("${searchService}/all?indexName=${instanceName}")
        dir.walkTopDown()
                .onEnter { it.name != ".git" }
                .filter { it.isFile }
                .filter { supportedExtensions.contains(it.extension) }
                .forEach { index(searchService, it.getMeta(dir, false).fullName) }
        return true
    }

    private fun index(searchService: URI, path: String) {
        val fileInfo = fileService.getFileInfo(path, false)
        val file = File(dir, fileInfo.fullName)
        restTemplate.postForLocation("${searchService}?indexName=${instanceName}&path=${path}", file.readText())
    }

    private fun getSearchService(): URI {
        val discoverClient = applicationContext.getBean(DiscoveryClient::class.java)
        val searches = discoverClient.getInstances("search")
        if (searches.isEmpty()) {
            throw RuntimeException("Search service is not available.")
        }
        return searches[0].uri
    }
}