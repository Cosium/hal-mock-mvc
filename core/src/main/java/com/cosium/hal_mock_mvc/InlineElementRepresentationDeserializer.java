package com.cosium.hal_mock_mvc;

import com.cosium.hal_mock_mvc.template.options.InlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.MapInlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.StringInlineElementRepresentation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.Map;

/**
 * @author Réda Housni Alaoui
 */
class InlineElementRepresentationDeserializer extends StdDeserializer<InlineElementRepresentation> {

  public InlineElementRepresentationDeserializer() {
    this(null);
  }

  protected InlineElementRepresentationDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public InlineElementRepresentation deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {

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
