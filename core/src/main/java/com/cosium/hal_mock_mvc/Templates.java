package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.OrMatcher.anyOf;
import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeType;

/**
 * @author Réda Housni Alaoui
 */
public class Templates {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final HalFormsBody body;

  Templates(RequestExecutor requestExecutor, ResultActions resultActions) throws Exception {
    this.requestExecutor = requireNonNull(requestExecutor);

    resultActions
        .andExpect(anyOf(status().is2xxSuccessful(), status().is3xxRedirection()))
        .andExpect(content().contentType(MediaTypes.HAL_FORMS_JSON));

    objectMapper =
        JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(new JacksonModule())
            .build();
    body =
        objectMapper.readValue(
            resultActions.andReturn().getResponse().getContentAsString(), HalFormsBody.class);
  }

  private Templates(RequestExecutor requestExecutor, ObjectMapper objectMapper, HalFormsBody body) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.objectMapper = requireNonNull(objectMapper);
    this.body = requireNonNull(body);
  }

  /**
   * @return The href of the resource's self link
   */
  public String selfHref() {
    return body.baseUri;
  }

  /**
   * @param relation The relation of a single resource embedded in this resource
   * @return The HAL-FORMS templates provided by the embedded resource
   */
  public Templates embedded(String relation) {
    JsonNode embeddedResource = findEmbedded(relation);
    if (embeddedResource.isObject()) {
      return buildEmbeddedTemplates(embeddedResource);
    }
    throw new AssertionError(
        "Embedded relation <%s> is <%s> instead of <%s>"
            .formatted(relation, embeddedResource.getNodeType(), JsonNodeType.OBJECT));
  }

  /**
   * @param relation The relation of an array of resources embedded in this resource
   * @return The HAL-FORMS templates provided by each embedded resource, in the array order
   */
  public List<Templates> embeddedList(String relation) {
    JsonNode embeddedResources = findEmbedded(relation);
    if (embeddedResources.isArray()) {
      return embeddedResources.valueStream().map(this::buildEmbeddedTemplates).toList();
    }
    throw new AssertionError(
        "Embedded relation <%s> is <%s> instead of <%s>"
            .formatted(relation, embeddedResources.getNodeType(), JsonNodeType.ARRAY));
  }

  private JsonNode findEmbedded(String relation) {
    return Optional.ofNullable(body.embeddedByRelation.get(relation))
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "No embedded resource found for relation <%s>".formatted(relation)));
  }

  private Templates buildEmbeddedTemplates(JsonNode embeddedResource) {
    return new Templates(
        requestExecutor,
        objectMapper,
        objectMapper.treeToValue(embeddedResource, HalFormsBody.class));
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
    private final Map<String, JsonNode> embeddedByRelation;

    @JsonCreator
    public HalFormsBody(
        @JsonProperty("_links") Map<String, Link> linkByName,
        @JsonProperty("_templates") Map<String, TemplateRepresentation> templateByKey,
        @JsonProperty("_embedded") Map<String, JsonNode> embeddedByRelation) {
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

      this.embeddedByRelation =
          Optional.ofNullable(embeddedByRelation)
              .map(Collections::unmodifiableMap)
              .orElseGet(Collections::emptyMap);
    }
  }

  /**
   * @author Réda Housni Alaoui
   */
  private record Link(@JsonProperty("href") String href) {}
}
