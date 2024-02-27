package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.hateoas.MediaTypes;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author Réda Housni Alaoui
 */
public class Templates {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final HalFormsBody body;

  Templates(RequestExecutor requestExecutor, ResultActions resultActions) throws Exception {
    this.requestExecutor = requireNonNull(requestExecutor);

    resultActions.andExpect(content().contentType(MediaTypes.HAL_FORMS_JSON));

    MvcResult mvcResult = resultActions.andReturn();
    MockHttpServletResponse response = mvcResult.getResponse();
    int responseStatus = response.getStatus();
    String jsonBody = response.getContentAsString();
    if (responseStatus < 200 || responseStatus >= 400) {
      throw new IllegalStateException(
          "GET on "
              + mvcResult.getRequest().getRequestURI()
              + " failed with code "
              + responseStatus
              + " and body '"
              + jsonBody
              + "'");
    }
    objectMapper =
        new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(new JacksonModule());
    body = objectMapper.readValue(jsonBody, HalFormsBody.class);
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
