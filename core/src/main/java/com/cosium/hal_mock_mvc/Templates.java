package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Réda Housni Alaoui
 */
public class Templates {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final HalFormsBody body;

  Templates(RequestExecutor requestExecutor, JsonNode resource) {
    this.requestExecutor = requireNonNull(requestExecutor);

    objectMapper =
        JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(new JacksonModule())
            .build();
    body = objectMapper.treeToValue(resource, HalFormsBody.class);
  }

  public Optional<Template> byOptionalKey(String key) {
    return Optional.ofNullable(body.templateByKey.get(key))
        .map(
            representation ->
                new Template(requestExecutor, objectMapper, body.baseUri, key, representation));
  }

  public Template byKey(String key) {
    return byOptionalKey(key)
        .orElseThrow(() -> new NoSuchElementException("No template found for key '" + key + "'"));
  }

  public Collection<Template> list() {
    return body.templateByKey.entrySet().stream()
        .map(
            entry ->
                new Template(
                    requestExecutor, objectMapper, body.baseUri, entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
  }

  private static class HalFormsBody {

    private final String baseUri;
    private final Map<String, TemplateRepresentation> templateByKey;

    @JsonCreator
    public HalFormsBody(
        @JsonProperty("_links") Map<String, Link> linkByName,
        @JsonProperty("_templates") Map<String, TemplateRepresentation> templateByKey) {
      baseUri =
          Optional.ofNullable(linkByName)
              .map(linkByNameMap -> linkByNameMap.get("self"))
              .map(Link::href)
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "_link.self is missing from the HAL forms representation"));

      this.templateByKey =
          Optional.ofNullable(templateByKey)
              .map(Collections::unmodifiableMap)
              .orElseGet(Collections::emptyMap);
    }
  }

  /**
   * @author Réda Housni Alaoui
   */
  private record Link(@JsonProperty("href") String href) {}
}
