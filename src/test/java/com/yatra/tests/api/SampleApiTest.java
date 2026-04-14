package com.yatra.tests.api;

import com.aventstack.extentreports.Status;
import com.playwright.core.api.ApiRequestManager;
import com.playwright.core.api.ApiResponse;
import com.playwright.utils.ExtentManager;
import com.playwright.utils.JsonDataReader;
import com.yatra.api.UserApi;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Sample API tests using reqres.in — demonstrates the API testing pattern.
 */
public class SampleApiTest extends BaseApiTest {

    @Test
    public void testGetUsers() {
        UserApi userApi = new UserApi(ApiRequestManager.getRequestContext());

        ApiResponse response = userApi.getUsers(1);
        ExtentManager.getTest().log(Status.INFO, "GET /api/users?page=1 → " + response.statusCode());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.jsonPath("data[0].email"));
    }

    @Test
    public void testGetUserById() {
        UserApi userApi = new UserApi(ApiRequestManager.getRequestContext());

        ApiResponse response = userApi.getUserById(2);
        ExtentManager.getTest().log(Status.INFO, "GET /api/users/2 → " + response.statusCode());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.jsonPath("data.first_name"));
    }

    @DataProvider(name = "createUserData")
    public Object[][] createUserData() {
        return JsonDataReader.getTestData("api-test-data.json", "createUserTests");
    }

    @Test(dataProvider = "createUserData")
    public void testCreateUser(Map<String, Object> data) {
        String name = (String) data.get("name");
        String job = (String) data.get("job");

        UserApi userApi = new UserApi(ApiRequestManager.getRequestContext());

        ApiResponse response = userApi.createUser(name, job);
        ExtentManager.getTest().log(Status.INFO, "POST /api/users → " + response.statusCode());

        Assert.assertEquals(response.statusCode(), 201);
        Assert.assertEquals(response.jsonBody().get("name").asText(), name);
        Assert.assertEquals(response.jsonBody().get("job").asText(), job);
        Assert.assertNotNull(response.jsonBody().get("id"));
    }

    @Test
    public void testUpdateUser() {
        UserApi userApi = new UserApi(ApiRequestManager.getRequestContext());

        ApiResponse response = userApi.updateUser(2, "Updated Name", "Updated Job");
        ExtentManager.getTest().log(Status.INFO, "PUT /api/users/2 → " + response.statusCode());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonBody().get("name").asText(), "Updated Name");
    }

    @Test
    public void testDeleteUser() {
        UserApi userApi = new UserApi(ApiRequestManager.getRequestContext());

        ApiResponse response = userApi.deleteUser(2);
        ExtentManager.getTest().log(Status.INFO, "DELETE /api/users/2 → " + response.statusCode());

        Assert.assertEquals(response.statusCode(), 204);
    }
}
