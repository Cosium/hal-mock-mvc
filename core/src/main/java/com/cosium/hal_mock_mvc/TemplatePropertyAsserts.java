package com.cosium.hal_mock_mvc;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * @author Réda Housni Alaoui
 */
public class TemplatePropertyAsserts {

  private TemplatePropertyAsserts() {}

  public static Consumer<TemplatePropertyAssert> hasRequired(boolean expected) {
    return propertyAssert -> propertyAssert.hasRequired(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasValue(@Nullable Object expected) {
    return propertyAssert -> propertyAssert.hasValue(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasRegex(@Nullable String expected) {
    return propertyAssert -> propertyAssert.hasRegex(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasTemplated(boolean expected) {
    return propertyAssert -> propertyAssert.hasTemplated(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasPrompt(@Nullable String expected) {
    return propertyAssert -> propertyAssert.hasPrompt(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasReadOnly(boolean expected) {
    return propertyAssert -> propertyAssert.hasReadOnly(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasType(@Nullable String expected) {
    return propertyAssert -> propertyAssert.hasType(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasMax(@Nullable Double expected) {
    return propertyAssert -> propertyAssert.hasMax(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasMin(@Nullable Double expected) {
    return propertyAssert -> propertyAssert.hasMin(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasMaxLength(@Nullable Long expected) {
    return propertyAssert -> propertyAssert.hasMaxLength(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasMinLength(@Nullable Long expected) {
    return propertyAssert -> propertyAssert.hasMinLength(expected);
  }

  public static Consumer<TemplatePropertyAssert> hasStep(@Nullable Double expected) {
    return propertyAssert -> propertyAssert.hasStep(expected);
  }

  public static Consumer<TemplatePropertyAssert> exists(boolean expected) {
    return propertyAssert -> propertyAssert.exists(expected);
  }
}
