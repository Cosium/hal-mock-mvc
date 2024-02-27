package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import java.util.List;

/**
 * @author Réda Housni Alaoui
 */
class TemplateOptionsInlineElements {
  private final String valueField;
  private final List<InlineElementRepresentation> representations;

  TemplateOptionsInlineElements(
      String valueField, List<InlineElementRepresentation> representations) {
    this.valueField = requireNonNull(valueField);
    this.representations = List.copyOf(representations);
  }

  public ValidatedFormProperty<String> validate(FormProperty<String> property) {

    if (representations.isEmpty()) {
      String firstValidationError =
          "Value of property '%s' must be null because the list of available option is empty."
              .formatted(property.name());

      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(firstValidationError);
    }

    List<TemplateOptionsInlineElement> inlineElements =
        representations.stream()
            .map(representation -> new TemplateOptionsInlineElement(valueField, representation))
            .toList();

    for (String value : property.values()) {

      if (inlineElements.stream().anyMatch(inlineElement -> inlineElement.matches(value))) {
        continue;
      }

      String firstValidationError =
          "Value '%s' didn't match any inline option of property '%s' among %s"
              .formatted(value, property.name(), representations);

      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(firstValidationError);
    }

    return ValidatedFormProperty.markAsValid(property);
  }
}
