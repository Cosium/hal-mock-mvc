package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.OptionsLinkRepresentation;
import com.cosium.hal_mock_mvc.template.options.OptionsRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.hateoas.Link;

/**
 * @author Réda Housni Alaoui
 */
class TemplateOptions {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final OptionsRepresentation representation;

  TemplateOptions(
      RequestExecutor requestExecutor,
      ObjectMapper objectMapper,
      OptionsRepresentation representation) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.objectMapper = requireNonNull(objectMapper);
    this.representation = requireNonNull(representation);
  }

  public <T> ValidatedFormProperty<T> validate(FormProperty<T> property) throws Exception {
    if (!String.class.equals(property.valueType())) {

      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "Value of type '%s' is not valid because the type should be of type 'String' given property '%s' expects a value from an enumeration of options."
                  .formatted(property.valueType(), property.name()));
    }

    long numberOfValues = property.values().size();

    Long maxItems = representation.maxItems().orElse(null);
    if (maxItems != null && numberOfValues > maxItems) {

      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "%s values passed for property '%s' while maxItems == %s"
                  .formatted(numberOfValues, property.name(), maxItems));
    }

    long minItems = representation.minItems();
    if (numberOfValues < minItems) {

      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "%s values passed for property '%s' while minItems == %s"
                  .formatted(numberOfValues, property.name(), minItems));
    }

    @SuppressWarnings("unchecked")
    FormProperty<String> stringProperty = (FormProperty<String>) property;

    String valueField = representation.valueField().orElse("value");

    List<InlineElementRepresentation> inlineElements = representation.inline().orElse(null);
    if (inlineElements != null) {
      return new TemplateOptionsInlineElements(valueField, inlineElements)
          .validate(stringProperty)
          .mapTo(property);
    }

    OptionsLinkRepresentation optionsLink = representation.link().orElse(null);
    if (optionsLink == null) {
      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(false)
          .reason(
              "Missing options inline and remote elements for property '%s'."
                  .formatted(property.name()));
    }

    return new TemplateOptionsInlineElements(valueField, fetchRemoteElements(optionsLink))
        .validate(stringProperty)
        .mapTo(property);
  }

  private List<InlineElementRepresentation> fetchRemoteElements(
      OptionsLinkRepresentation optionsLink) throws Exception {

    String optionsHref = Link.of(optionsLink.href()).expand().toUri().toString();

    String rawOptions =
        requestExecutor
            .execute(get(optionsHref))
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return objectMapper.readValue(rawOptions, new TypeReference<>() {});
  }
}
