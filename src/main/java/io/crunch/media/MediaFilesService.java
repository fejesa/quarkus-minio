package io.crunch.media;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@ApplicationScoped
public class MediaFilesService implements MediaFiles {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaFileRepository mediaFileRepository;

    public MediaFilesService(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    public long store(String mediaId, String mediaType) {
        var mediaFile = new MediaFile();
        mediaFile.setMediaId(mediaId);
        mediaFile.setMediaType(mediaType);
        mediaFileRepository.persistAndFlush(mediaFile);
        logger.info("Media file {} is stored", mediaFile.getMediaId());
        return mediaFile.getId();
    }

    @Override
    public Optional<MediaFile> getByMediaId(String mediaId) {
        return mediaFileRepository.findByMediaId(mediaId);
    }
}
