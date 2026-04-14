package com.playwright.core.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Lightweight wrapper around Playwright's APIResponse.
 * Provides convenient methods for status checks, body parsing, and JSON extraction.
 */
public class ApiResponse {

    private static final Logger logger = LogManager.getLogger(ApiResponse.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final APIResponse response;
    private String bodyText;
    private JsonNode bodyJson;

    public ApiResponse(APIResponse response) {
        this.response = response;
    }

    public int statusCode() {
        return response.status();
    }

    public String statusText() {
        return response.statusText();
    }

    public boolean isOk() {
        return response.ok();
    }

    public String body() {
        if (bodyText == null) {
            bodyText = new String(response.body());
        }
        return bodyText;
    }

    public JsonNode jsonBody() {
        if (bodyJson == null) {
            try {
                bodyJson = mapper.readTree(body());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response body as JSON: " + e.getMessage(), e);
            }
        }
        return bodyJson;
    }

    /**
     * Extract a value from JSON response using a path like "data.name" or "users[0].email".
     */
    public JsonNode jsonPath(String path) {
        JsonNode node = jsonBody();
        for (String key : path.split("\\.")) {
            if (node == null) return null;
            if (key.contains("[")) {
                String field = key.substring(0, key.indexOf('['));
                int index = Integer.parseInt(key.substring(key.indexOf('[') + 1, key.indexOf(']')));
                node = node.get(field);
                if (node != null && node.isArray()) {
                    node = node.get(index);
                }
            } else {
                node = node.get(key);
            }
        }
        return node;
    }

    public String header(String name) {
        Map<String, String> headers = response.headers();
        return headers.get(name.toLowerCase());
    }

    public Map<String, String> headers() {
        return response.headers();
    }

    public <T> T bodyAs(Class<T> clazz) {
        try {
            return mapper.readValue(body(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response body: " + e.getMessage(), e);
        }
    }

    public <T> List<T> bodyAsListOf(Class<T> clazz) {
        try {
            return mapper.readValue(body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response body as list: " + e.getMessage(), e);
        }
    }

    public void dispose() {
        response.dispose();
    }

    @Override
    public String toString() {
        return "ApiResponse{status=" + statusCode() + ", url=" + response.url() + "}";
    }
}
