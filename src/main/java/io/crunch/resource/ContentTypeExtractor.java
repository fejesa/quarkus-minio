package io.crunch.resource;

import io.crunch.shared.MediaFileServerException;
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

/**
 * A service for extracting the content type (MIME type) of media files.
 * <p>
 * This class utilizes <a href="https://tika.apache.org/">Apache Tika</a> to detect the content type based on file metadata and content analysis.
 * It also includes special handling for QuickTime files to accurately distinguish MP4 format.
 * </p>
 * <p>
 * The class is annotated with {@code @ApplicationScoped}, making it a singleton bean in the application context.
 * </p>
 */
@ApplicationScoped
public class ContentTypeExtractor {

    /**
     * Determines the content type of a given file.
     * <p>
     * If the detected content type is {@code video/quicktime}, the method further inspects
     * the file to determine whether it is an MP4 file.
     * </p>
     *
     * @param path         the path to the file
     * @param originalName the original file name (used as metadata for type detection)
     * @return the detected MIME type of the file
     * @throws MediaFileServerException if an error occurs during content type detection
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

    /**
     * Detects the file type based on metadata and content analysis using Apache Tika.
     *
     * @param path         the path to the file
     * @param originalName the original file name (used for metadata-based detection)
     * @return the detected MIME type of the file
     * @throws IOException   if an I/O error occurs while reading the file
     * @throws TikaException if an error occurs within Apache Tika during type detection
     */
    private String detectFileType(Path path, String originalName) throws IOException, TikaException {
        try (var inputStream = new FileInputStream(path.toFile());
             var tikaInputStream = TikaInputStream.get(inputStream)) {
            var tc = new TikaConfig();
            var md = new Metadata();
            md.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalName);
            return tc.getDetector().detect(tikaInputStream, md).toString();
        }
    }

    /**
     * Checks whether a QuickTime file is actually an MP4 file.
     * <p>
     * If the initial detection returns {@code video/quicktime}, this method further parses
     * the file using an {@link AutoDetectParser} to extract the precise MIME type.
     * </p>
     *
     * @param path the path to the file
     * @return the refined MIME type (e.g., {@code video/mp4} if applicable)
     * @throws IOException   if an I/O error occurs while reading the file
     * @throws TikaException if an error occurs within Apache Tika during parsing
     * @throws SAXException  if an error occurs while processing the parsed metadata
     */
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
