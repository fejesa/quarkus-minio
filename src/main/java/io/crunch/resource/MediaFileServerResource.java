package io.crunch.resource;

import io.crunch.media.MediaFiles;
import io.crunch.store.MediaFileStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * RESTful API resource for handling media file uploads and retrieval.
 * <p>
 * This class provides endpoints to upload media files and fetch stored media IDs.
 * It ensures data integrity by validating file checksums before storage.
 * </p>
 * <p>
 * The resource utilizes Jakarta RESTful Web Services (JAX-RS) with RESTEasy Reactive for handling
 * multipart file uploads. The uploaded files are validated, assigned a unique media ID,
 * and stored persistently.
 * </p>
 * @apiNote The endpoints are executed in blocking mode to simplify the implementation.
 */
@Path("/api")
public class MediaFileServerResource {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaFileChecksumGenerator checksumGenerator;

    private final ContentTypeExtractor contentTypeExtractor;

    private final MediaUrls mediaUrls;

    private final MediaFileStore mediaFileStore;

    private final MediaFiles mediaFiles;

    public MediaFileServerResource(MediaFileChecksumGenerator checksumGenerator, ContentTypeExtractor contentTypeExtractor,
                                   MediaUrls mediaUrls, MediaFileStore mediaFileStore, MediaFiles mediaFiles) {
        this.checksumGenerator = checksumGenerator;
        this.contentTypeExtractor = contentTypeExtractor;
        this.mediaUrls = mediaUrls;
        this.mediaFileStore = mediaFileStore;
        this.mediaFiles = mediaFiles;
    }

    /**
     * Handles the upload of a media file.
     * <p>
     * The uploaded file is validated using its checksum before being stored. It also checks the content type of the file.
     * If the validation succeeds, a unique media ID is generated, and the file is stored with its content type.
     * </p>
     * Note: In a production environment, additional security measures should be implemented to prevent
     * unauthorized access, and to ensure infection-free file uploads.
     *
     * @param mediaFile             the uploaded media file (cannot be null)
     * @param mediaFileDescription  metadata associated with the media file, including checksum validation
     * @return a response containing the URL of the stored media file
     * @throws BadRequestException if the checksum validation fails or any unexpected error occurs
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RestResponse<String> createMediaFile(
            @NotNull
            @RestForm("media") FileUpload mediaFile,
            @RestForm("description") @PartType(MediaType.APPLICATION_JSON) @Valid MediaFileDescription mediaFileDescription) {
        try {
            logger.info("Media file upload request with params {}", mediaFileDescription);
            validateChecksum(mediaFile, mediaFileDescription);

            var contentType = contentTypeExtractor.getContentType(mediaFile.filePath(), mediaFile.fileName());
            checkContentType(contentType);
            var url = mediaUrls.createUrl();
            var mediaId = mediaUrls.getMediaId(url);

            logger.info("Generated media id: {}, and content type: {}", mediaId, contentType);

            mediaFiles.store(mediaId, contentType);
            mediaFileStore.store(mediaFile.filePath(), mediaId, contentType);

            return RestResponse.status(Response.Status.CREATED, url);
        } catch (Exception e) {
            logger.error("Error storing media file", e);
            throw new BadRequestException();
        }
    }

    /**
     * Retrieves a list of stored media file IDs.
     * <p>
     * The response contains a list of URLs that can be used to access the stored media files.
     * </p>
     *
     * @return a response containing the list of media file URLs
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<List<String>> getMediaIds() {
        return RestResponse.ok(mediaFiles.getMediaIds()
                .stream()
                .map(mediaUrls::createUrl)
                .toList());
    }

    /**
     * Validates the checksum of an uploaded media file.
     * <p>
     * The computed checksum of the file is compared with the expected checksum provided in the request metadata.
     * If the values do not match, an error is logged, and a {@code BadRequestException} is thrown.
     * </p>
     *
     * @param mediaFile            the uploaded media file
     * @param mediaFileDescription the metadata containing the expected checksum
     * @throws BadRequestException if the checksum validation fails
     */
    private void validateChecksum(FileUpload mediaFile, MediaFileDescription mediaFileDescription) {
        var checksum = checksumGenerator.checksum(mediaFile.filePath());
        if (!mediaFileDescription.checksum().equals(checksum)) {
            logger.error("Media file {} checksum error", mediaFile.fileName());
            throw new BadRequestException();
        }
    }

    private void checkContentType(String contentType) {
        boolean isSupported = switch (contentType) {
            case "image/jpeg", "image/png", "audio/mpeg", "application/pdf", "video/mp4" -> true;
            default -> false;
        };
        if (!isSupported) {
            throw new BadRequestException("Unsupported content type: " + contentType);
        }
    }
}
