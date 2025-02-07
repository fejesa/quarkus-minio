package io.crunch.media.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class MediaFileNameGeneratorTest {

    @Inject
    MediaFileNameGenerator mediaFileNameGenerator;

    @Test
    void returnNoEmptyFileName() {
        var fileName = mediaFileNameGenerator.generate();
        assertThat(fileName).isNotNull().isNotEmpty().hasSize(32);
    }

    @Test
    void checkUniqueFileNames() {
        var limit = 10_000;
        var ids = Stream.generate(mediaFileNameGenerator::generate).limit(limit).collect(Collectors.toSet());
        assertThat(ids).hasSize(limit);
    }
}
