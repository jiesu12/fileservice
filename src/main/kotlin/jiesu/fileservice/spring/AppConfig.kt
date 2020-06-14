package jiesu.fileservice.spring

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import java.io.File

@Configuration
class AppConfig {
    @Bean
    fun getBasedir(@Value("\${fileservice.path.file}") dir: String) = File(dir)

    @Bean
    fun indexCoordinates(@Value("\${eureka.instance.metadataMap.name}") instanceName: String) =
            IndexCoordinates.of(instanceName)
}