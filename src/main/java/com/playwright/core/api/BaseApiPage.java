package com.playwright.core.api;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.options.RequestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Abstract base for API service classes (analogous to BasePage for UI).
 * Each API service/endpoint group extends this and gets HTTP methods for free.
 *
 * Example:
 *   public class UserApi extends BaseApiPage {
 *       public ApiResponse getUser(int id) {
 *           return get("/users/" + id);
 *       }
 *   }
 */
public abstract class BaseApiPage {

    private static final Logger logger = LogManager.getLogger(BaseApiPage.class);
    protected final APIRequestContext request;

    public BaseApiPage(APIRequestContext request) {
        this.request = request;
    }

    protected ApiResponse get(String endpoint) {
        logger.info("GET {}", endpoint);
        return new ApiResponse(request.get(endpoint));
    }

    protected ApiResponse get(String endpoint, Map<String, Object> queryParams) {
        logger.info("GET {} with params: {}", endpoint, queryParams);
        RequestOptions options = RequestOptions.create();
        queryParams.forEach(options::setQueryParam);
        return new ApiResponse(request.get(endpoint, options));
    }

    protected ApiResponse post(String endpoint, Object body) {
        logger.info("POST {}", endpoint);
        return new ApiResponse(request.post(endpoint, RequestOptions.create().setData(body)));
    }

    protected ApiResponse put(String endpoint, Object body) {
        logger.info("PUT {}", endpoint);
        return new ApiResponse(request.put(endpoint, RequestOptions.create().setData(body)));
    }

    protected ApiResponse patch(String endpoint, Object body) {
        logger.info("PATCH {}", endpoint);
        return new ApiResponse(request.patch(endpoint, RequestOptions.create().setData(body)));
    }

    protected ApiResponse delete(String endpoint) {
        logger.info("DELETE {}", endpoint);
        return new ApiResponse(request.delete(endpoint));
    }

    protected ApiResponse postWithHeaders(String endpoint, Object body, Map<String, String> headers) {
        logger.info("POST {} with custom headers", endpoint);
        RequestOptions options = RequestOptions.create().setData(body);
        headers.forEach(options::setHeader);
        return new ApiResponse(request.post(endpoint, options));
    }
}
