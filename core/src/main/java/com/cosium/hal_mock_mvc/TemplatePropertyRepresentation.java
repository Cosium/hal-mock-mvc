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
  private final boolean readOnly;
  private final String type;
  private final Double max;
  private final Long maxLength;
  private final Double min;
  private final Long minLength;
  private final Double step;

  @JsonCreator
  TemplatePropertyRepresentation(
      @JsonProperty("name") String name,
      @JsonProperty("required") Boolean required,
      @JsonProperty("value") String value,
      @JsonProperty("prompt") String prompt,
      @JsonProperty("regex") String regex,
      @JsonProperty("templated") Boolean templated,
      @JsonProperty("options") OptionsRepresentation options,
      @JsonProperty("readOnly") Boolean readOnly,
      @JsonProperty("type") String type,
      @JsonProperty("max") Double max,
      @JsonProperty("maxLength") Long maxLength,
      @JsonProperty("min") Double min,
      @JsonProperty("minLength") Long minLength,
      @JsonProperty("step") Double step) {
    this.name = requireNonNull(name, "Attribute 'name' is missing");
    this.required = Optional.ofNullable(required).orElse(false);
    this.value = value;
    this.prompt = Optional.ofNullable(prompt).orElse(name);
    this.regex = regex;
    this.templated = Optional.ofNullable(templated).orElse(false);
    this.options = options;
    this.readOnly = Optional.ofNullable(readOnly).orElse(false);
    this.type = Optional.ofNullable(type).orElse("text");
    this.max = max;
    this.maxLength = maxLength;
    this.min = min;
    this.minLength = minLength;
    this.step = step;
  }

  public String name() {
    return name;
  }

  public String type() {
    return type;
  }

  public boolean readOnly() {
    return readOnly;
  }

  public boolean required() {
    return required;
  }

  public Optional<String> value() {
    return Optional.ofNullable(value);
  }

  public Optional<Double> max() {
    return Optional.ofNullable(max);
  }

  public Optional<Long> maxLength() {
    return Optional.ofNullable(maxLength);
  }

  public Optional<Double> min() {
    return Optional.ofNullable(min);
  }

  public Optional<Long> minLength() {
    return Optional.ofNullable(minLength);
  }

  public Optional<Double> step() {
    return Optional.ofNullable(step);
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
