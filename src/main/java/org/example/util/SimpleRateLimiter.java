package org.example.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public class SimpleRateLimiter {

    private static final int delay = 1_000;

    private final Semaphore semaphore;

    public SimpleRateLimiter(int count) {
        semaphore = new Semaphore(count);
    }

    public <T> T rateLimit(Callable<T> callable) {
        try {
            semaphore.acquire();
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }
}
