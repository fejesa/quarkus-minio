package io.crunch.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MediaFileDescription(@JsonProperty("checksum") @NotBlank @Size(max = 255) String checksum) {
}
