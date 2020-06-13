package jiesu.fileservice.spring

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class AppConfig {
    @Bean
    fun getBasedir(@Value("\${fileservice.path.file}") dir: String) = File(dir)


}