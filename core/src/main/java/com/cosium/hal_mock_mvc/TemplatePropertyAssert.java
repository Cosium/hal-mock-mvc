package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * @author Réda Housni Alaoui
 */
public class TemplatePropertyAssert {

  private final String propertyName;
  private final @Nullable TemplatePropertyRepresentation property;

  TemplatePropertyAssert(String propertyName, @Nullable TemplatePropertyRepresentation property) {
    this.propertyName = requireNonNull(propertyName);
    this.property = property;
  }

  public TemplatePropertyAssert hasRequired(boolean expected) {
    assertEquals("Property required", expected, requireProperty().required());
    return this;
  }

  public TemplatePropertyAssert hasValue(@Nullable Object expected) {
    assertEquals("Property value", expected, requireProperty().rawValue().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasRegex(@Nullable String expected) {
    assertEquals("Property regex", expected, requireProperty().regex().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasTemplated(boolean expected) {
    assertEquals("Property templated", expected, requireProperty().templated());
    return this;
  }

  public TemplatePropertyAssert hasPrompt(@Nullable String expected) {
    assertEquals("Property prompt", expected, requireProperty().prompt());
    return this;
  }

  public TemplatePropertyAssert hasReadOnly(boolean expected) {
    assertEquals("Property readOnly", expected, requireProperty().readOnly());
    return this;
  }

  public TemplatePropertyAssert hasType(@Nullable String expected) {
    assertEquals("Property type", expected, requireProperty().type());
    return this;
  }

  public TemplatePropertyAssert hasMax(@Nullable Double expected) {
    assertEquals("Property max", expected, requireProperty().max().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMin(@Nullable Double expected) {
    assertEquals("Property min", expected, requireProperty().min().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMaxLength(@Nullable Long expected) {
    assertEquals("Property maxLength", expected, requireProperty().maxLength().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasMinLength(@Nullable Long expected) {
    assertEquals("Property minLength", expected, requireProperty().minLength().orElse(null));
    return this;
  }

  public TemplatePropertyAssert hasStep(@Nullable Double expected) {
    assertEquals("Property step", expected, requireProperty().step().orElse(null));
    return this;
  }

  public TemplatePropertyAssert exists(boolean expected) {
    assertEquals("Property exists", expected, property != null);
    return this;
  }

  private TemplatePropertyRepresentation requireProperty() {
    return Objects.requireNonNull(
        property, "No property found for name <%s>".formatted(propertyName));
  }
}
