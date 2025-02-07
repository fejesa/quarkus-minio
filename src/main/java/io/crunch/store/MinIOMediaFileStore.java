package io.crunch.store;

import io.crunch.shared.MediaFileServerException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

/**
 * Implementation of {@link MediaFileStore} that uses <a href="https://min.io/">MinIO</a> for object storage.
 * <p>
 * This class provides methods for storing, retrieving, and checking file metadata in MinIO.
 * It ensures that the required bucket exists upon application startup.
 * </p>
 */
@ApplicationScoped
public class MinIOMediaFileStore implements MediaFileStore {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MinioClient minioClient;

    private final String bucketName;

    /**
     * Constructs a {@code MinIOMediaFileStore} with a MinIO client and the target bucket name.
     *
     * @param minioClient MinIO client for interacting with the object store.
     * @param bucketName  The name of the MinIO bucket where media files are stored.
     */
    public MinIOMediaFileStore(MinioClient minioClient, @ConfigProperty(name = "minio.bucket-name") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    /**
     * Stores a file in the MinIO bucket.
     *
     * @param path        The local file path of the file to be uploaded.
     * @param fileName    The name of the file in MinIO.
     * @param contentType The MIME type of the file.
     * @throws MediaFileServerException if an error occurs during file upload.
     */
    @Override
    public void store(Path path, String fileName, String contentType) {
        try {
            logger.info("Storing file: {}", fileName);
            var args = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .filename(path.toString())
                    .contentType(contentType)
                    .build();
            var response = minioClient.uploadObject(args);
            logger.info("Upload file {} response: {}", fileName, response.etag());
        } catch (Exception e) {
            logger.error("Error storing file", e);
            throw new MediaFileServerException("Error storing file", e);
        }
    }

    /**
     * Retrieves a file from the MinIO bucket.
     *
     * @param fileName The name of the file in MinIO.
     * @return An {@link InputStream} to read the file content.
     * @throws MediaFileServerException if an error occurs while reading the file.
     */
    @Override
    public InputStream read(String fileName) {
        try {
            logger.info("Reading file: {}", fileName);
            var args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build();
            return minioClient.getObject(args);
        } catch (Exception e) {
            logger.error("Error reading file", e);
            throw new MediaFileServerException("Error reading file", e);
        }
    }

    /**
     * Retrieves the size of a file stored in MinIO.
     *
     * @param fileName The name of the file in MinIO.
     * @return The size of the file in bytes.
     * @throws MediaFileServerException if an error occurs while fetching file metadata.
     */
    @Override
    public long getFileSize(String fileName) {
        try {
            logger.info("Getting file size: {}", fileName);
            var args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build();
            var stat = minioClient.statObject(args);
           return stat.size();
        } catch (Exception e) {
            logger.error("Error getting file size", e);
            throw new MediaFileServerException("Error getting file size", e);
        }
    }

    /**
     * Ensures that the MinIO bucket exists. If not, it is created.
     * This method is called automatically after the application starts.
     */
    @PostConstruct
    void initStore() {
        if (!bucketExists()) {
            createBucket();
        }
    }

    /**
     * Creates the MinIO bucket if it does not already exist.
     *
     * @throws MediaFileServerException if an error occurs while creating the bucket.
     */
    private void createBucket()  {
        try {
            logger.info("Creating bucket: {}", bucketName);
            var args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.makeBucket(args);
        } catch (Exception e) {
            logger.error("Error creating bucket", e);
            throw new MediaFileServerException("Error creating bucket", e);
        }
    }

    /**
     * Checks whether the MinIO bucket exists.
     *
     * @return {@code true} if the bucket exists, {@code false} otherwise.
     * @throws MediaFileServerException if an error occurs while checking for the bucket.
     */
    private boolean bucketExists() {
        try {
            logger.info("Checking if bucket exists: {}", bucketName);
            var args = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();
            return minioClient.bucketExists(args);
        } catch (Exception e) {
            logger.error("Error checking if bucket exists", e);
            throw new MediaFileServerException("Error checking if bucket exists", e);
        }
    }
}
