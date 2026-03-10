package com.example.elk.controller;

import com.example.elk.service.LogGeneratorService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogGeneratorService logGeneratorService;

    public LogController(LogGeneratorService logGeneratorService) {
        this.logGeneratorService = logGeneratorService;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info(@RequestParam(defaultValue = "rest-api") String source) {
        String traceId = logGeneratorService.generateInfoLog(source);
        return ResponseEntity.ok(Map.of("status", "INFO generated", "traceId", traceId));
    }

    @GetMapping("/warn")
    public ResponseEntity<Map<String, String>> warn(@RequestParam(defaultValue = "rest-api") String source) {
        String traceId = logGeneratorService.generateWarnLog(source);
        return ResponseEntity.ok(Map.of("status", "WARN generated", "traceId", traceId));
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> error(@RequestParam(defaultValue = "rest-api") String source) {
        String traceId = logGeneratorService.generateErrorLog(source);
        return ResponseEntity.ok(Map.of("status", "ERROR generated", "traceId", traceId));
    }
}
