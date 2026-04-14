package com.yatra.api;

import com.microsoft.playwright.APIRequestContext;
import com.playwright.core.api.ApiResponse;
import com.playwright.core.api.BaseApiPage;

import java.util.Map;

/**
 * Sample API service for reqres.in — demonstrates the API Page Object pattern.
 * Replace with your actual API endpoints.
 */
public class UserApi extends BaseApiPage {

    public UserApi(APIRequestContext request) {
        super(request);
    }

    public ApiResponse getUsers(int page) {
        return get("/api/users", Map.of("page", page));
    }

    public ApiResponse getUserById(int id) {
        return get("/api/users/" + id);
    }

    public ApiResponse createUser(String name, String job) {
        return post("/api/users", Map.of("name", name, "job", job));
    }

    public ApiResponse updateUser(int id, String name, String job) {
        return put("/api/users/" + id, Map.of("name", name, "job", job));
    }

    public ApiResponse deleteUser(int id) {
        return delete("/api/users/" + id);
    }
}
