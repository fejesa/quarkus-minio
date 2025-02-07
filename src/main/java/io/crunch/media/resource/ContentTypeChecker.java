package io.crunch.media.resource;

import io.crunch.media.shared.MediaFileServerException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class ContentTypeChecker {

    /**
     * Detects the content type of the provided file. If the type is <i>quicktime</i> then it parses the file to determine the correct format.
     */
    public String getContentType(Path path, String originalName) throws MediaFileServerException {
        try {
            var contentType = detectFileType(path, originalName);
            if ("video/quicktime".equals(contentType)) {
                contentType = checkMP4(path);
            }
            return contentType;
        } catch (IOException | TikaException | SAXException e) {
            throw new MediaFileServerException("Error detecting content type", e);
        }
    }

    private String detectFileType(Path path, String originalName) throws IOException, TikaException {
        try (var inputStream = new FileInputStream(path.toFile());
             var tikaInputStream = TikaInputStream.get(inputStream)) {
            var tc = new TikaConfig();
            var md = new Metadata();
            md.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalName);
            return tc.getDetector().detect(tikaInputStream, md).toString();
        }
    }

    private String checkMP4(Path path) throws IOException, TikaException, SAXException {
        try (var inputStream = new FileInputStream(path.toFile());
             var tikaInputStream = TikaInputStream.get(inputStream)) {
            var parser = new AutoDetectParser();
            var metadata = new Metadata();
            parser.parse(tikaInputStream, new BodyContentHandler(), metadata, new ParseContext());
            return metadata.get(HttpHeaders.CONTENT_TYPE);
        }
    }
}
