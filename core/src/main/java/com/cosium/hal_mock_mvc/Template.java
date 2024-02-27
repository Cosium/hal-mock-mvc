package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Réda Housni Alaoui
 */
public class Template implements SubmittableTemplate {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final String key;
  private final TemplateRepresentation representation;

  private final String httpMethod;
  private final URI target;

  Template(
      RequestExecutor requestExecutor,
      ObjectMapper objectMapper,
      String baseUri,
      String key,
      TemplateRepresentation representation) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.objectMapper = requireNonNull(objectMapper);
    this.key = requireNonNull(key);
    this.representation = requireNonNull(representation);

    httpMethod = representation.method().toUpperCase();
    target = URI.create(representation.target().orElse(baseUri));
  }

  public Form createForm() {
    return new Form(requestExecutor, objectMapper, this);
  }

  @Override
  public HalMockMvc createAndShift() throws Exception {
    return createAndShift(null);
  }

  /**
   * Submits the template by expecting a 201 Created response then begins a new traversal starting
   * at the returned Location header.
   *
   * @param content The content to submit
   */
  public HalMockMvc createAndShift(String content) throws Exception {
    return requestExecutor.assertCreatedAndShift(submit(content));
  }

  @Override
  public ResultActions submit() throws Exception {
    return submit(null);
  }

  /**
   * Submits the template with the provided content.
   *
   * @param content The content to submit
   */
  public ResultActions submit(String content) throws Exception {
    MockHttpServletRequestBuilder requestBuilder =
        MockMvcRequestBuilders.request(httpMethod, target);
    if (content != null) {
      requestBuilder = requestBuilder.contentType(representation.contentType()).content(content);
    }
    return requestExecutor.execute(requestBuilder);
  }

  /** Begins a Template multipart submission. */
  public TemplateMultipartRequest multipart() {
    return new TemplateMultipartRequest(requestExecutor, httpMethod, target);
  }

  /**
   * @return The template key
   */
  public String key() {
    return key;
  }

  /**
   * @return The template representation
   */
  public TemplateRepresentation representation() {
    return representation;
  }
}
