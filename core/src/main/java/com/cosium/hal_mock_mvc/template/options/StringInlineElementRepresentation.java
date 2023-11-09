package com.cosium.hal_mock_mvc.template.options;

import static java.util.Objects.requireNonNull;

/**
 * @author Réda Housni Alaoui
 */
public record StringInlineElementRepresentation(String value)
    implements InlineElementRepresentation {

  public StringInlineElementRepresentation {
    requireNonNull(value);
  }
}
