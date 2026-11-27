package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import tools.jackson.databind.JsonNode;

/**
 * A HAL resource embedded in another one.
 *
 * @author Réda Housni Alaoui
 */
public class EmbeddedResource {

  private final RequestExecutor requestExecutor;
  private final JsonNode resource;

  EmbeddedResource(RequestExecutor requestExecutor, JsonNode resource) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.resource = requireNonNull(resource);
  }

  /**
   * @return The href of the resource's self link
   */
  public String selfHref() {
    JsonNode selfHref = resource.at("/_links/self/href");
    if (selfHref.isString()) {
      return selfHref.asString();
    }
    throw new NoSuchElementException("_links.self.href is missing from the embedded resource");
  }

  /**
   * @return The HAL-FORMS templates provided by the resource
   */
  public Templates templates() {
    return new Templates(requestExecutor, resource);
  }
}
