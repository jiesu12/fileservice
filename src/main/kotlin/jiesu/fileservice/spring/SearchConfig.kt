package jiesu.fileservice.spring

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

@Configuration
class SearchConfig(@Value("\${elasticsearch.host}") val elasticSearchHost: String,
                   @Value("\${eureka.instance.metadataMap.name}") val instanceName: String
) : AbstractElasticsearchConfiguration() {

    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder().connectedTo(elasticSearchHost).build()
        return RestClients.create(clientConfiguration).rest()
    }

    @Bean
    fun indexCoordinates() = IndexCoordinates.of(instanceName)
}
