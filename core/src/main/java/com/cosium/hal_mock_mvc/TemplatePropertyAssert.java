package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import org.jspecify.annotations.Nullable;

/**
 * @author Réda Housni Alaoui
 */
public class TemplatePropertyAssert {

  private final TemplatePropertyRepresentation property;

  TemplatePropertyAssert(TemplatePropertyRepresentation property) {
    this.property = requireNonNull(property);
  }

  public TemplatePropertyAssert hasRequired(boolean expected) {
    assertEquals("Property required", expected, property.required());
    return this;
  }

  public TemplatePropertyAssert hasValue(@Nullable String expected) {
    assertEquals("Property value", expected, property.value().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasRegex(@Nullable String expected) {
    assertEquals("Property regex", expected, property.regex().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasTemplated(boolean expected) {
    assertEquals("Property templated", expected, property.templated());
    return this;
  }

  public TemplatePropertyAssert hasPrompt(@Nullable String expected) {
    assertEquals("Property prompt", expected, property.prompt());
    return this;
  }

  public TemplatePropertyAssert hasReadOnly(boolean expected) {
    assertEquals("Property readOnly", expected, property.readOnly());
    return this;
  }

  public TemplatePropertyAssert hasType(@Nullable String expected) {
    assertEquals("Property type", expected, property.type());
    return this;
  }

  public TemplatePropertyAssert hasMax(@Nullable Double expected) {
    assertEquals("Property max", expected, property.max().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMin(@Nullable Double expected) {
    assertEquals("Property min", expected, property.min().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMaxLength(@Nullable Long expected) {
    assertEquals("Property maxLength", expected, property.maxLength().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMinLength(@Nullable Long expected) {
    assertEquals("Property minLength", expected, property.minLength().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasStep(@Nullable Double expected) {
    assertEquals("Property step", expected, property.step().orElse(null));
    return this;
  }
}
