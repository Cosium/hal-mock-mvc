package com.cosium.hal_mock_mvc;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Réda Housni Alaoui
 */
class JacksonModule extends SimpleModule {

  public JacksonModule() {
    addDeserializer(
        InlineElementRepresentation.class, new InlineElementRepresentationDeserializer());
  }

  @Override
  public String getModuleName() {
    return "com.cosium.hal_mock_mvc";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}
