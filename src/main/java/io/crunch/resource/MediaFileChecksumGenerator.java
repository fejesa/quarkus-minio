package io.crunch.resource;

import io.crunch.shared.MediaFileServerException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates a checksum value of the provided file by using SHA256 hash algorithm.
 */
@ApplicationScoped
public class MediaFileChecksumGenerator {

    public String checksum(Path path) {
        try (var fis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            return DigestUtils.sha256Hex(fis);
        } catch (IOException e) {
            throw new MediaFileServerException(e);
        }
    }
}
