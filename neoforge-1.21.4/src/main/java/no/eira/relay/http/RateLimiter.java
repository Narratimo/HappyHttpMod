package no.eira.relay.http;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Per-IP rate limiter using sliding window algorithm.
 * Tracks request timestamps and enforces configurable rate limits.
 */
public class RateLimiter {

    private final Map<String, List<Long>> requestTimes = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMs;

    /**
     * Create a rate limiter
     * @param maxRequests Maximum requests allowed in the time window
     * @param windowMs Time window in milliseconds
     */
    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Check if a request from the given IP is allowed
     * @param clientIp The client's IP address
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isAllowed(String clientIp) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        List<Long> times = requestTimes.computeIfAbsent(clientIp, k -> new CopyOnWriteArrayList<>());

        // Remove old entries outside the window
        times.removeIf(time -> time < windowStart);

        // Check if under limit
        if (times.size() < maxRequests) {
            times.add(now);
            return true;
        }

        return false;
    }

    /**
     * Get the time in milliseconds until the client can make another request
     * @param clientIp The client's IP address
     * @return Milliseconds until next allowed request, or 0 if allowed now
     */
    public long getRetryAfterMs(String clientIp) {
        List<Long> times = requestTimes.get(clientIp);
        if (times == null || times.isEmpty()) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        // Remove old entries
        times.removeIf(time -> time < windowStart);

        if (times.size() < maxRequests) {
            return 0;
        }

        // Find oldest request in the window
        long oldest = times.stream().min(Long::compare).orElse(now);
        long waitUntil = oldest + windowMs;
        return Math.max(0, waitUntil - now);
    }

    /**
     * Get remaining requests for the client
     * @param clientIp The client's IP address
     * @return Number of remaining requests in the current window
     */
    public int getRemainingRequests(String clientIp) {
        List<Long> times = requestTimes.get(clientIp);
        if (times == null) {
            return maxRequests;
        }

        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        // Count requests in window
        long count = times.stream().filter(time -> time >= windowStart).count();
        return Math.max(0, maxRequests - (int) count);
    }

    /**
     * Clean up old entries from all clients
     * Should be called periodically to prevent memory leaks
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        Iterator<Map.Entry<String, List<Long>>> it = requestTimes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Long>> entry = it.next();
            List<Long> times = entry.getValue();
            times.removeIf(time -> time < windowStart);
            if (times.isEmpty()) {
                it.remove();
            }
        }
    }

    /**
     * Get configuration info
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowMs() {
        return windowMs;
    }
}
