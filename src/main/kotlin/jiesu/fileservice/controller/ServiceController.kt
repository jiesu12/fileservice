package jiesu.fileservice.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/service")
class ServiceController(val discoveryClient: DiscoveryClient,
                        @Value("\${spring.application.name}") val serviceName: String) {

    /**
     * Get all fileservice instances that have registered at eureka discovery.
     */
    @GetMapping("/instances")
    fun getInstances(): List<String> = discoveryClient.getInstances(serviceName).mapNotNull { it.metadata["name"] }
}