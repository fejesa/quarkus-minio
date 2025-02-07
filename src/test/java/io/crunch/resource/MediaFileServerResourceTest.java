package io.crunch.resource;

import io.crunch.store.MediaFileStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MediaFileServerResourceTest {

    @Inject
    MediaFileChecksumGenerator checksumGenerator;

    @Inject
    MediaFileUrl mediaFileUrl;

    @Inject
    MediaFileStore mediaFileStore;

    @Inject
    EntityManager entityManager;

    @Test
    void uploadMediaFileSuccessfully() throws Exception {
        var sampleMediaFile = getSampleMediaFile("/sample-audio.mp3");
        var checksum = checksumGenerator.checksum(sampleMediaFile);

        var response = given()
            .multiPart("description", new MediaFileDescription(checksum), MediaType.APPLICATION_JSON)
            .multiPart("media", sampleMediaFile.toFile(), MediaType.APPLICATION_OCTET_STREAM)
            .post("/api")
            .then()
            .log().all()
            .body(containsString("localhost:8080/media"))
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract()
            .response();

        var mediaId = mediaFileUrl.getMediaId(response.getBody().asString());
        try (var inputStream = mediaFileStore.read(mediaId)) {
            assertThat(inputStream).isNotNull();
        }

        var query = "select f from MediaFile f where f.mediaId = '%s'".formatted(mediaId);
        assertThat(entityManager.createQuery(query).getSingleResult()).isNotNull();
    }

    private Path getSampleMediaFile(String path) throws URISyntaxException {
        var url = MediaFileServerResourceTest.class.getResource(path);
        return Path.of(Objects.requireNonNull(url).toURI());
    }
}
