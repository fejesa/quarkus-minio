package io.crunch.resource;

import io.crunch.media.MediaFiles;
import io.crunch.store.MediaFileStore;
import jakarta.inject.Inject;
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

@Path("/api")
public class MediaFileServerResource {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    MediaFileChecksumGenerator checksumGenerator;

    @Inject
    ContentTypeExtractor contentTypeExtractor;

    @Inject
    MediaUrls mediaUrls;

    @Inject
    MediaFileStore mediaFileStore;

    @Inject
    MediaFiles mediaFiles;

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
            var url = mediaUrls.createUrl();
            var mediaId = mediaUrls.getMediaId(url);

            logger.info("Generated media id: {}, and content type: {}", mediaId, contentType);

            mediaFiles.store(mediaId, contentType);
            mediaFileStore.store(mediaFile.filePath(), mediaId, contentType);

            return RestResponse.status(Response.Status.CREATED, url);
        } catch (BadRequestException | NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error storing media file", e);
            throw new BadRequestException();
        }
    }

    private void validateChecksum(FileUpload mediaFile, MediaFileDescription mediaFileDescription) {
        var checksum = checksumGenerator.checksum(mediaFile.filePath());
        if (!mediaFileDescription.checksum().equals(checksum)) {
            logger.error("Media file {} checksum error", mediaFile.fileName());
            throw new BadRequestException();
        }
    }
}
