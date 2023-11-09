package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.cosium.hal_mock_mvc.template.options.OptionsRepresentation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
public class TemplatePropertyRepresentation {
  private final String name;
  private final boolean required;
  private final String value;
  private final String prompt;
  private final String regex;
  private final boolean templated;
  private final OptionsRepresentation options;

  @JsonCreator
  TemplatePropertyRepresentation(
      @JsonProperty("name") String name,
      @JsonProperty("required") Boolean required,
      @JsonProperty("value") String value,
      @JsonProperty("prompt") String prompt,
      @JsonProperty("regex") String regex,
      @JsonProperty("templated") Boolean templated,
      @JsonProperty("options") OptionsRepresentation options) {
    this.name = requireNonNull(name, "Attribute 'name' is missing");
    this.required = Optional.ofNullable(required).orElse(false);
    this.value = value;
    this.prompt = Optional.ofNullable(prompt).orElse(name);
    this.regex = regex;
    this.templated = Optional.ofNullable(templated).orElse(false);
    this.options = options;
  }

  public String name() {
    return name;
  }

  public boolean required() {
    return required;
  }

  public Optional<String> value() {
    return Optional.ofNullable(value);
  }

  public String prompt() {
    return prompt;
  }

  public Optional<String> regex() {
    return Optional.ofNullable(regex);
  }

  public boolean templated() {
    return templated;
  }

  public Optional<OptionsRepresentation> options() {
    return Optional.ofNullable(options);
  }
}
