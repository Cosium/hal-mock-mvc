package com.cosium.hal_mock_mvc;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.MapInlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.StringInlineElementRepresentation;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * @author Réda Housni Alaoui
 */
class InlineElementRepresentationDeserializer
    extends ValueDeserializer<InlineElementRepresentation> {

  @Override
  public InlineElementRepresentation deserialize(JsonParser p, DeserializationContext ctxt)
      throws JacksonException {

    JsonToken currentToken = p.currentToken();
    if (currentToken == JsonToken.START_OBJECT) {
      Map<String, String> map = p.readValueAs(new StringStringMap());
      if (map == null) {
        return null;
      }
      return new MapInlineElementRepresentation(map);
    }
    if (currentToken == JsonToken.VALUE_STRING) {
      String value = p.readValueAs(String.class);
      if (value == null) {
        return null;
      }
      return new StringInlineElementRepresentation(value);
    }

    throw MismatchedInputException.from(
        p,
        InlineElementRepresentation.class,
        "%s should have been either %s or %s. But it is not."
            .formatted(currentToken, JsonToken.START_OBJECT, JsonToken.VALUE_STRING));
  }

  private static class StringStringMap extends TypeReference<Map<String, String>> {}
}
