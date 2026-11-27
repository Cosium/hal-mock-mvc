package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.OrMatcher.anyOf;
import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        @JsonProperty("_links") @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            Map<String, List<Link>> linksByRelation,
        @JsonProperty("_templates") Map<String, TemplateRepresentation> templateByKey) {

      List<Link> selfLinks =
          Optional.ofNullable(linksByRelation)
              .map(linksByRelationMap -> linksByRelationMap.get("self"))
              .orElseGet(List::of);
      if (selfLinks.isEmpty()) {
        throw new NoSuchElementException("_link.self is missing from the HAL forms representation");
      }
      if (selfLinks.size() > 1) {
        throw new AssertionError(
            "Resource has <%s> self links instead of <1>".formatted(selfLinks.size()));
      }
      baseUri = selfLinks.get(0).href();

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
