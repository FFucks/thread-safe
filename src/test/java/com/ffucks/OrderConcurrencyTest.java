package com.ffucks;

import com.ffucks.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyTest {

    @Autowired
    OrderService service;

    @Test
    void manyThreadsGetUniqueIds() throws Exception {
        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        CompletionService<Long> cs = new ExecutorCompletionService<>(pool);

        for (int i = 0; i < threads; i++) {
            int n = i;
            cs.submit(() -> {
                start.await();
                var saved = service.createOrder("CODE-" + n);
                return saved.getId();
            });
        }
        start.countDown();

        Set<Long> ids = new java.util.HashSet<>();
        for (int i = 0; i < threads; i++) {
            ids.add(cs.take().get(10, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(ids).hasSize(threads);
        assertThat(ids).allMatch(id -> id != null);
    }
}
