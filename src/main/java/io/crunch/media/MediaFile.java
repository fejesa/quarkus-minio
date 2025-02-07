package io.crunch.media;

import jakarta.persistence.*;

@Entity
@Table(name = "MEDIA_FILE")
public class MediaFile {

    @Id
    @SequenceGenerator(name = "id_gen", sequenceName = "id_sequence", allocationSize = 1)
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    /** Uniquely identifies the media file for the public, a.k.a. the query param of the public URL */
    @Column(name = "media_id", updatable = false, nullable = false, length = 512, unique = true)
    private String mediaId;

    /** Type of the media file, for example audio/mpeg. */
    @Column(name = "content_type", updatable = false, nullable = false, length = 255)
    private String mediaType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
