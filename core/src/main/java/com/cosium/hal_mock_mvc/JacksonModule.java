package com.cosium.hal_mock_mvc;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleDeserializers;

/**
 * @author Réda Housni Alaoui
 */
class JacksonModule extends tools.jackson.databind.JacksonModule {

  @Override
  public String getModuleName() {
    return "com.cosium.hal_mock_mvc";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }

  @Override
  public void setupModule(SetupContext context) {
    context.addDeserializers(
        new SimpleDeserializers()
            .addDeserializer(
                InlineElementRepresentation.class, new InlineElementRepresentationDeserializer()));
  }
}
