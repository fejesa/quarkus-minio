package io.crunch.viewer;

/**
 * Manages temporary mappings between generated tokens and media file identifiers.
 * <p>
 * This interface provides a caching mechanism for secure, time-limited access to media files.
 * When a client accesses a media file via a public URL, a temporary token is generated, stored
 * in both the client's cookie and the cache, and used to redirect the request to the appropriate
 * viewer URL. The cached token allows retrieval of the media ID, which is then used to fetch the
 * corresponding media file from the storage.
 * </p>
 * <p>
 * Implementations of this interface can support token Time-To-Live (TTL) and are automatically invalidated after expiration.
 * </p>
 */
public interface MediaRequestCache {

    /**
     * Associates a media file ID with a generated authentication token.
     * <p>
     * This method securely stores the relationship between a token and a media ID,
     * allowing subsequent retrieval of the media file based on the provided token.
     * </p>
     *
     * @param token   The unique authentication token generated for accessing the media file.
     * @param mediaId The identifier of the media file associated with the token.
     */
    void putMediaId(String token, String mediaId);

    /**
     * Retrieves and removes the media file ID associated with the given token.
     * <p>
     * This method fetches the media ID linked to a specific token. Since tokens are meant
     * for one-time use, the associated media ID is removed from the cache upon retrieval.
     * If the token has already expired or does not exist, this method returns {@code null}.
     * </p>
     *
     * @param token The authentication token used to retrieve the associated media file ID.
     * @return The media file ID associated with the token, or {@code null} if the token is invalid or expired.
     * @apiNote Tokens are automatically removed from the cache upon expiration.
     */
    String getMediaId(String token);

    /**
     * Verifies whether a given token is still valid.
     * <p>
     * This method checks if a token exists in the cache and has not expired.
     * A valid token allows retrieval of the associated media file ID and subsequent access
     * to the media content.
     * </p>
     *
     * @param token The authentication token to be verified.
     * @return {@code true} if the token is valid and has not expired, {@code false} otherwise.
     */
    boolean isValidToken(String token);
}
