package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import jakarta.servlet.http.Part;
import java.net.URI;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author Réda Housni Alaoui
 */
public class MultipartRequest {

  private final RequestExecutor requestExecutor;
  private final MockMultipartHttpServletRequestBuilder requestBuilder;

  MultipartRequest(RequestExecutor requestExecutor, URI uri) {
    this.requestExecutor = requireNonNull(requestExecutor);
    requireNonNull(uri, "Expected a non null get URI");
    requestBuilder = MockMvcRequestBuilders.multipart(uri);
  }

  public MultipartRequest file(String name, byte[] content) {
    requestBuilder.file(name, content);
    return this;
  }

  public MultipartRequest file(MockMultipartFile file) {
    requestBuilder.file(file);
    return this;
  }

  public MultipartRequest part(Part... parts) {
    requestBuilder.part(parts);
    return this;
  }

  public ResultActions post() throws Exception {
    return execute("post");
  }

  public ResultActions put() throws Exception {
    return execute("put");
  }

  private ResultActions execute(String httpMethod) throws Exception {
    requestBuilder.with(
        request -> {
          request.setMethod(httpMethod.toUpperCase());
          return request;
        });
    return requestExecutor.execute(requestBuilder);
  }
}
