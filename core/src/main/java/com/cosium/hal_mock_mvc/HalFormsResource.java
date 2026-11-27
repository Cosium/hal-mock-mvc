package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.NoSuchElementException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

/**
 * A HAL-FORMS resource.
 *
 * @author Réda Housni Alaoui
 */
public class HalFormsResource {

  private final RequestExecutor requestExecutor;
  private final JsonNode resource;

  HalFormsResource(RequestExecutor requestExecutor, JsonNode resource) {
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
    throw new NoSuchElementException("_links.self.href is missing from the resource");
  }

  /**
   * @return The HAL-FORMS templates provided by the resource
   */
  public Templates templates() {
    return new Templates(requestExecutor, resource);
  }

  /**
   * @param relation The relation of a single resource embedded in this resource
   * @return The embedded resource
   */
  public HalFormsResource embedded(String relation) {
    JsonNode embeddedResource = findEmbedded(relation);
    if (embeddedResource.isObject()) {
      return new HalFormsResource(requestExecutor, embeddedResource);
    }
    throw new AssertionError(
        "Embedded relation <%s> is <%s> instead of <%s>"
            .formatted(relation, embeddedResource.getNodeType(), JsonNodeType.OBJECT));
  }

  /**
   * @param relation The relation of an array of resources embedded in this resource
   * @return The embedded resources, in the array order
   */
  public List<HalFormsResource> embeddedList(String relation) {
    JsonNode embeddedResources = findEmbedded(relation);
    if (embeddedResources.isArray()) {
      return embeddedResources
          .valueStream()
          .map(embeddedResource -> new HalFormsResource(requestExecutor, embeddedResource))
          .toList();
    }
    throw new AssertionError(
        "Embedded relation <%s> is <%s> instead of <%s>"
            .formatted(relation, embeddedResources.getNodeType(), JsonNodeType.ARRAY));
  }

  private JsonNode findEmbedded(String relation) {
    JsonNode embedded = resource.path("_embedded").path(relation);
    if (!embedded.isMissingNode()) {
      return embedded;
    }
    throw new NoSuchElementException(
        "No embedded resource found for relation <%s>".formatted(relation));
  }
}
