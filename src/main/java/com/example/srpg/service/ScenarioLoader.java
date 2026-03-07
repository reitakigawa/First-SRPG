package com.example.srpg.service;

import com.example.srpg.domain.ScenarioConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScenarioLoader {
    private final ObjectMapper objectMapper;

    public ScenarioLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ScenarioConfig load(String scenarioName) {
        String resourcePath = "scenarios/" + scenarioName + ".json";
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return objectMapper.readValue(resource.getInputStream(), ScenarioConfig.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Scenario not found or invalid: " + scenarioName, e);
        }
    }
}
