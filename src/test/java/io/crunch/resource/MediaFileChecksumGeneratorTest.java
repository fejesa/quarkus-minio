package io.crunch.resource;

import io.crunch.shared.MediaFileServerException;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaFileChecksumGeneratorTest {

    @Test
    void generateChecksumWhenFileExists() throws URISyntaxException {
        var url = MediaFileChecksumGeneratorTest.class.getResource("/sample-pdf.pdf");
        var path = Path.of(Objects.requireNonNull(url).toURI());
        var generator = new MediaFileChecksumGenerator();
        assertThat(generator.checksum(path)).isEqualTo("38c9792d725c45dd431699e6a3b0f0f8e17c63c9ac7331387ee30dcc6e42a511");
    }

    @Test
    void throwsErrorIfFileDoesNotExists() {
        var generator = new MediaFileChecksumGenerator();
        var path = Path.of("does-not-exist");
        assertThatThrownBy(() -> generator.checksum(path)).isInstanceOf(MediaFileServerException.class);
    }
}
