package io.crunch.media.store;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Defines the API for storing and accessing media files.
 */
public interface MediaFileStore {

    /**
     * Moves the given file to the permanent storage.<p>
     * The uploaded file is saved by the Quarkus Rest in a temporal file that is deleted when the request terminates.
     *
     * @param path     The absolute path of the uploaded file
     * @param fileName The unique name of the file that should be saved.
     *                 The name should be unique to avoid overwriting existing files.
     * @param contentType The content type of the file.
     */
    void store(Path path, String fileName, String contentType);

    /**
     * Reads the file represented by <code>fileName</code> and returns an input stream.
     *
     * @param fileName The path of the file to read.
     * @return An input stream of the file content.
     * @apiNote By calling this method, the caller is responsible for closing the input stream.
     */
    InputStream read(String fileName);

    /**
     * Gets the size of the requested file.
     *
     * @param fileName The name of the file
     * @return Size of the given file in bytes.
     */
    long getFileSize(String fileName);
}
