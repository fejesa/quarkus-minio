package io.crunch.media;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class MediaFileRepository implements PanacheRepository<MediaFile> {

    public Optional<MediaFile> findByMediaId(String mediaId) {
        return find("mediaId", mediaId).firstResultOptional();
    }
}
