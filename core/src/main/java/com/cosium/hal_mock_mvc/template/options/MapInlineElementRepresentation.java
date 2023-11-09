package com.cosium.hal_mock_mvc.template.options;

import java.util.Map;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
public record MapInlineElementRepresentation(Map<String, String> map)
    implements InlineElementRepresentation {

  public MapInlineElementRepresentation(Map<String, String> map) {
    this.map = Optional.ofNullable(map).map(Map::copyOf).orElseGet(Map::of);
  }
}
