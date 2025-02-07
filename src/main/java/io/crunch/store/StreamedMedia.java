package io.crunch.store;

import org.primefaces.model.StreamedContent;

public record StreamedMedia(StreamedContent content, String extension) {
}
