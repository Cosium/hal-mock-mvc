package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import org.jspecify.annotations.Nullable;

/**
 * @author Réda Housni Alaoui
 */
public class TemplateAssert {

  private final TemplateRepresentation template;

  TemplateAssert(TemplateRepresentation template) {
    this.template = requireNonNull(template);
  }

  public TemplateAssert hasTitle(@Nullable String expected) {
    assertEquals("Template title", expected, template.title().orElse(null));
    return this;
  }
}
