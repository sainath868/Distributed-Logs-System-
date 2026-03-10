package com.example.elk.service;

import java.time.Instant;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class LogGeneratorService {

    private static final Logger logger = LogManager.getLogger(LogGeneratorService.class);

    public String generateInfoLog(String source) {
        String traceId = UUID.randomUUID().toString();
        logger.info("event=API_CALL level=INFO source={} traceId={} message=Info log generated at {}",
                source, traceId, Instant.now());
        return traceId;
    }

    public String generateWarnLog(String source) {
        String traceId = UUID.randomUUID().toString();
        logger.warn("event=API_CALL level=WARN source={} traceId={} message=Warning log generated at {}",
                source, traceId, Instant.now());
        return traceId;
    }

    public String generateErrorLog(String source) {
        String traceId = UUID.randomUUID().toString();
        try {
            int value = 10 / 0;
            logger.info("event=API_CALL level=INFO source={} traceId={} value={}", source, traceId, value);
        } catch (ArithmeticException ex) {
            logger.error("event=API_CALL level=ERROR source={} traceId={} message=Error while processing request",
                    source, traceId, ex);
        }
        return traceId;
    }
}
