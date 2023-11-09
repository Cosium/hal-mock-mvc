package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;

/**
 * @author Réda Housni Alaoui
 */
public class TemplateRepresentation {

  private final String title;
  private final String method;
  private final String contentType;
  private final Map<String, TemplatePropertyRepresentation> propertyByName;
  private final String target;

  @JsonCreator
  TemplateRepresentation(
      @JsonProperty("title") String title,
      @JsonProperty("method") String method,
      @JsonProperty("contentType") String contentType,
      @JsonProperty("properties") List<TemplatePropertyRepresentation> properties,
      @JsonProperty("target") String target) {
    this.title = title;
    this.method = requireNonNull(method, "Attribute 'method' is missing");
    this.contentType = Optional.ofNullable(contentType).orElse(MediaType.APPLICATION_JSON_VALUE);
    Map<String, TemplatePropertyRepresentation> mutablePropertyByName =
        Optional.ofNullable(properties).orElseGet(Collections::emptyList).stream()
            .collect(
                Collectors.toMap(
                    TemplatePropertyRepresentation::name,
                    Function.identity(),
                    (e1, e2) -> {
                      throw new IllegalStateException(
                          "Properties " + e1 + " and " + e2 + " have the same name");
                    },
                    LinkedHashMap::new));
    this.propertyByName = Collections.unmodifiableMap(mutablePropertyByName);
    this.target = target;
  }

  public Optional<String> title() {
    return Optional.ofNullable(title);
  }

  public String method() {
    return method;
  }

  public String contentType() {
    return contentType;
  }

  public Map<String, TemplatePropertyRepresentation> propertyByName() {
    return propertyByName;
  }

  public Optional<String> target() {
    return Optional.ofNullable(target);
  }
}
