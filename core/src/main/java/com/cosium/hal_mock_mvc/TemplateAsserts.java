package com.cosium.hal_mock_mvc;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * @author Réda Housni Alaoui
 */
public class TemplateAsserts {

  private TemplateAsserts() {}

  public static Consumer<TemplateAssert> hasTitle(@Nullable String expected) {
    return templateAssert -> templateAssert.hasTitle(expected);
  }
}
