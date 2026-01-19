package no.eira.relay.http.api;

/**
 * Result of an HTTP request containing status code and response body.
 */
public record HttpResult(int statusCode, String body) {

    /**
     * Check if the response indicates success (2xx status).
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Check if the response indicates a client error (4xx status).
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if the response indicates a server error (5xx status).
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * Check if the response indicates a redirect (3xx status).
     */
    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }

    /**
     * Convert status code to comparator signal strength (0-15).
     * - 15: Success (2xx)
     * - 10: Redirect (3xx)
     * - 5: Client Error (4xx)
     * - 2: Server Error (5xx)
     * - 0: No response / timeout / other
     */
    public int toComparatorSignal() {
        if (statusCode == 0) return 0;
        if (isSuccess()) return 15;
        if (isRedirect()) return 10;
        if (isClientError()) return 5;
        if (isServerError()) return 2;
        return 0;
    }

    /**
     * Create a result representing a failed/timeout request.
     */
    public static HttpResult failure() {
        return new HttpResult(0, "");
    }

    /**
     * Create a result from just a body (assumes success if body is non-empty).
     * Used for backwards compatibility.
     */
    public static HttpResult fromBody(String body) {
        return new HttpResult(body != null && !body.isEmpty() ? 200 : 0, body != null ? body : "");
    }
}
