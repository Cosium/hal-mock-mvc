package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.MapInlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.StringInlineElementRepresentation;

/**
 * @author Réda Housni Alaoui
 */
class TemplateOptionsInlineElement {

  private final String valueField;
  private final InlineElementRepresentation representation;

  TemplateOptionsInlineElement(String valueField, InlineElementRepresentation representation) {
    this.valueField = requireNonNull(valueField);
    this.representation = requireNonNull(representation);
  }

  public boolean matches(String value) {
    if (representation instanceof MapInlineElementRepresentation mapInlineElementRepresentation) {

      return value.equals(mapInlineElementRepresentation.map().get(valueField));

    } else if (representation
        instanceof StringInlineElementRepresentation stringInlineElementRepresentation) {

      return value.equals(stringInlineElementRepresentation.value());

    } else {
      throw new IllegalArgumentException("Unexpected type %s".formatted(representation.getClass()));
    }
  }
}
