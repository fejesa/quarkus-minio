package io.crunch.store;

import io.crunch.media.shared.MediaFileServerException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

@ApplicationScoped
public class MinIOMediaFileStore implements MediaFileStore {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MinioClient minioClient;

    private final String bucketName;

    public MinIOMediaFileStore(MinioClient minioClient, @ConfigProperty(name = "minio.bucket-name") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public void store(Path path, String fileName, String contentType) {
        try {
            logger.info("Storing file: {}", fileName);
            var uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .filename(path.toString())
                    .contentType(contentType)
                    .build();
            var response = minioClient.uploadObject(uploadObjectArgs);
            logger.info("Upload file {} response: {}", fileName, response.etag());
        } catch (Exception e) {
            logger.error("Error storing file", e);
            throw new MediaFileServerException("Error storing file", e);
        }
    }

    @Override
    public InputStream read(String fileName) {
        try {
            logger.info("Reading file: {}", fileName);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            logger.error("Error reading file", e);
            throw new MediaFileServerException("Error reading file", e);
        }
    }

    @Override
    public long getFileSize(String fileName) {
        try {
            logger.info("Getting file size: {}", fileName);
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
           return stat.size();
        } catch (Exception e) {
            logger.error("Error getting file size", e);
            throw new MediaFileServerException("Error getting file size", e);
        }
    }

    @PostConstruct
    void initStore() {
        if (!bucketExists()) {
            createBucket();
        }
    }

    private void createBucket()  {
        try {
            logger.info("Creating bucket: {}", bucketName);
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            logger.error("Error creating bucket", e);
            throw new MediaFileServerException("Error creating bucket", e);
        }
    }

    private boolean bucketExists() {
        try {
            logger.info("Checking if bucket exists: {}", bucketName);
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            logger.error("Error checking if bucket exists", e);
            throw new MediaFileServerException("Error checking if bucket exists", e);
        }
    }
}
