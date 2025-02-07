package io.crunch.viewer;

public interface MediaRequestCache {

    /**
     * Saves the media id associated with the token.
     *
     * @param token   The token to save.
     * @param mediaId The media id to save.
     */
    void putMediaId(String token, String mediaId);

    /**
     * Gets the media id associated with the token.
     *
     * @param token The token to get the media id for.
     * @return {@code true} if the token is removed, {@code false} otherwise
     * @apiNote The token is automatically removed from the cache after the TTL expires.
     */
    String getMediaId(String token);

    /**
     * Checks if a token is valid.
     *
     * @param token The token to check.
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    boolean isValidToken(String token);
}
