package com.cosium.hal_mock_mvc.template.options;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
public class OptionsLinkRepresentation {

  private final String href;
  private final String type;
  private final boolean templated;

  @JsonCreator
  public OptionsLinkRepresentation(
      @JsonProperty("href") String href,
      @JsonProperty("type") String type,
      @JsonProperty("templated") Boolean templated) {
    this.href = requireNonNull(href, "Attribute 'href' is missing");
    this.type = type;
    this.templated = Optional.ofNullable(templated).orElse(false);
  }

  public String href() {
    return href;
  }

  public Optional<String> type() {
    return Optional.ofNullable(type);
  }

  public boolean templated() {
    return templated;
  }
}
