package com.yegor256.tojos;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonoTojoTest {

    private final int N_THREADS = 10;
    private final ExecutorService service = Executors.newFixedThreadPool(N_THREADS);
    private TjDefault tojos;
    private MnJson mono;

    @BeforeEach
    void setUp(@TempDir final Path temp) {
        mono = new MnJson(temp.resolve("mono.json"));
        tojos = new TjDefault(mono);
    }

    @Test
    void setsConcurrentlyToTheSameMono() throws InterruptedException {
        service.invokeAll(
            IntStream.range(0, N_THREADS)
                .mapToObj(String::valueOf)
                .map(tojos::add)
                .map(tojo -> Executors.callable(() -> {
                    tojo.set(tojo.toString(), tojo.toString());
                }))
                .collect(Collectors.toList())
        );
        service.shutdown();
        service.awaitTermination(20, TimeUnit.SECONDS);
        final Collection<Map<String, String>> result = mono.read();
        MatcherAssert.assertThat(result, Matchers.hasSize(N_THREADS));
        for (final Map<String, String> tojo : result) {
            MatcherAssert.assertThat(tojo.entrySet(), Matchers.hasSize(2));
        }
    }

}