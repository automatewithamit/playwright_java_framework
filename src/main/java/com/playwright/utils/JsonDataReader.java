package com.playwright.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads JSON test data files from src/test/resources/testdata/.
 *
 * Usage with TestNG DataProvider:
 *
 *   @DataProvider
 *   public Object[][] flightData() {
 *       return JsonDataReader.getTestData("flight-search.json", "flightSearchTests");
 *   }
 *
 *   @Test(dataProvider = "flightData")
 *   public void testFlight(Map<String, Object> data) {
 *       String city = (String) data.get("departureCity");
 *   }
 */
public class JsonDataReader {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TEST_DATA_PATH = "testdata/";

    public static Object[][] getTestData(String fileName, String arrayKey) {
        try {
            JsonNode rootNode = readJsonFile(fileName);
            JsonNode arrayNode = rootNode.get(arrayKey);

            if (arrayNode == null || !arrayNode.isArray()) {
                throw new RuntimeException("Key '" + arrayKey + "' not found or not an array in " + fileName);
            }

            List<Map<String, Object>> testDataList = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                Map<String, Object> dataMap = new LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> {
                    JsonNode value = entry.getValue();
                    if (value.isInt()) {
                        dataMap.put(entry.getKey(), value.intValue());
                    } else if (value.isBoolean()) {
                        dataMap.put(entry.getKey(), value.booleanValue());
                    } else if (value.isDouble()) {
                        dataMap.put(entry.getKey(), value.doubleValue());
                    } else {
                        dataMap.put(entry.getKey(), value.asText());
                    }
                });
                testDataList.add(dataMap);
            }

            Object[][] result = new Object[testDataList.size()][1];
            for (int i = 0; i < testDataList.size(); i++) {
                result[i][0] = testDataList.get(i);
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data from " + fileName + ": " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> getTestDataByIndex(String fileName, String arrayKey, int index) {
        Object[][] allData = getTestData(fileName, arrayKey);
        if (index >= allData.length) {
            throw new RuntimeException("Index " + index + " out of bounds. Total records: " + allData.length);
        }
        return (Map<String, Object>) allData[index][0];
    }

    private static JsonNode readJsonFile(String fileName) {
        String filePath = TEST_DATA_PATH + fileName;
        try (InputStream is = JsonDataReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new RuntimeException("Test data file not found: " + filePath);
            }
            return mapper.readTree(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file: " + filePath, e);
        }
    }
}
