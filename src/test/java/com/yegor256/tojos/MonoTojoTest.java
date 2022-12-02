package com.yegor256.tojos;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonoTojoTest {

    @Test
    void setsConcurrentlyToTheSameMono(@TempDir Path temp) throws InterruptedException {
        final MnJson mono = new MnJson(temp.resolve("mono.json"));
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService service = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            final Tojo tojo = new TjDefault(mono).add(String.valueOf(i));
            final String kv = String.valueOf(i);
            service.submit(Executors.callable(() -> {
                try {
                    latch.await();
                    tojo.set(kv, kv);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }));
        }
        latch.countDown();
        service.shutdown();
        if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("");
        }
        final Collection<Map<String, String>> read = mono.read();
        assertEquals(10, read.size());
        assertTrue(read.stream().noneMatch(Map::isEmpty));
    }

}