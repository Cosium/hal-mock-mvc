package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Réda Housni Alaoui
 */
public class Request {

  private final RequestExecutor requestExecutor;
  private final MockHttpServletRequestBuilder requestBuilder;

  Request(RequestExecutor requestExecutor, URI uri) {
    this.requestExecutor = requireNonNull(requestExecutor);
    requireNonNull(uri, "Expected a non null get URI");
    requestBuilder = MockMvcRequestBuilders.request("get", uri);
  }

  public Request contentType(MediaType mediaType) {
    requestBuilder.contentType(mediaType);
    return this;
  }

  public Request content(String content) {
    requestBuilder.content(content);
    return this;
  }

  public Request content(byte[] content) {
    requestBuilder.content(content);
    return this;
  }

  public Request jsonContent(String content) {
    requestBuilder.contentType(MediaType.APPLICATION_JSON);
    content(content);
    return this;
  }

  public ResultActions get() throws Exception {
    return perform("get");
  }

  public ResultActions post() throws Exception {
    return perform("post");
  }

  public ResultActions put() throws Exception {
    return perform("put");
  }

  public ResultActions patch() throws Exception {
    return perform("patch");
  }

  public ResultActions delete() throws Exception {
    return perform("delete");
  }

  private ResultActions perform(String httpMethod) throws Exception {
    requestBuilder.with(
        request -> {
          request.setMethod(httpMethod.toUpperCase());
          return request;
        });
    return requestExecutor.execute(requestBuilder);
  }
}
