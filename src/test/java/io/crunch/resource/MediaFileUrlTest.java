package io.crunch.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MediaFileUrlTest {

    @Inject
    MediaFileUrl mediaFileUrl;

    @Test
    void getDifferentUUUIDWhenCalledMore() {
        var limit = 1_000_000;
        var links = Stream.generate(mediaFileUrl::createUrl).limit(limit).collect(Collectors.toSet());
        assertThat(links)
            .hasSize(limit)
            .extracting(String::length)
            .containsOnly(links.iterator().next().length());
    }
}
