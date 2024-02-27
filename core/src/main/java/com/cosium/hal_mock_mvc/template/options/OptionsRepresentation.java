package com.cosium.hal_mock_mvc.template.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
public class OptionsRepresentation {

  private final List<InlineElementRepresentation> inline;
  private final OptionsLinkRepresentation link;
  private final Long maxItems;
  private final long minItems;
  private final String promptField;
  private final List<String> selectedValues;
  private final String valueField;

  @JsonCreator
  public OptionsRepresentation(
      @JsonProperty("inline") List<InlineElementRepresentation> inline,
      @JsonProperty("link") OptionsLinkRepresentation link,
      @JsonProperty("maxItems") Long maxItems,
      @JsonProperty("minItems") Long minItems,
      @JsonProperty("promptField") String promptField,
      @JsonProperty("selectedValues") List<String> selectedValues,
      @JsonProperty("valueField") String valueField) {
    this.inline = Optional.ofNullable(inline).map(List::copyOf).orElse(null);
    this.link = link;
    this.maxItems = maxItems;
    this.minItems = Optional.ofNullable(minItems).orElse(0L);
    this.promptField = promptField;
    this.selectedValues = Optional.ofNullable(selectedValues).map(List::copyOf).orElseGet(List::of);
    this.valueField = valueField;
  }

  public Optional<List<InlineElementRepresentation>> inline() {
    return Optional.ofNullable(inline);
  }

  public Optional<OptionsLinkRepresentation> link() {
    return Optional.ofNullable(link);
  }

  public Optional<Long> maxItems() {
    return Optional.ofNullable(maxItems);
  }

  public long minItems() {
    return minItems;
  }

  public Optional<String> promptField() {
    return Optional.ofNullable(promptField);
  }

  public List<String> selectedValues() {
    return selectedValues;
  }

  public Optional<String> valueField() {
    return Optional.ofNullable(valueField);
  }
}
