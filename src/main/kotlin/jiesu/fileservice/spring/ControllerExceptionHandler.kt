package jiesu.fileservice.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(value = [RuntimeException::class])
    fun runtimeException(e: RuntimeException): ResponseEntity<String> {
        log.error("Controller threw runtime exception", e)
        return ResponseEntity(getTwoLevelsOfErrorMessages(e), HttpStatus.BAD_REQUEST)
    }

    private fun getTwoLevelsOfErrorMessages(e: Exception): String {
        val ss = StringBuilder(e.message)
        if (e.cause != null) {
            ss.append("\n")
            ss.append(e.cause!!.message)
        }
        return ss.toString()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}