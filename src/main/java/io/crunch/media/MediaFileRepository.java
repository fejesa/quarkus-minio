package io.crunch.media;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

/**
 * Repository for managing {@link MediaFile} entities.
 * <p>
 * This class provides database access methods for media files, using
 * Quarkus Panache to simplify persistence operations.
 * </p>
 * <p>
 * The repository is marked as {@code @ApplicationScoped}, meaning it is a singleton
 * within the application. It is also annotated with {@code @Transactional}, ensuring
 * that all methods execute within a transactional context.
 * </p>
 */
@ApplicationScoped
@Transactional
public class MediaFileRepository implements PanacheRepository<MediaFile> {

    /**
     * Finds a media file by its unique media ID.
     * <p>
     * Queries the database for a {@link MediaFile} entity that matches the given
     * {@code mediaId}. If no matching entity is found, an empty {@code Optional} is returned.
     * </p>
     *
     * @param mediaId the unique identifier of the media file
     * @return an {@code Optional} containing the media file if found, otherwise an empty {@code Optional}
     */
    public Optional<MediaFile> findByMediaId(String mediaId) {
        return find("mediaId", mediaId).firstResultOptional();
    }
}
