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
public class TemplateMultipartRequest implements SubmittableTemplate {

  private final RequestExecutor requestExecutor;
  private final MockMultipartHttpServletRequestBuilder requestBuilder;

  TemplateMultipartRequest(RequestExecutor requestExecutor, String httpMethod, URI uri) {
    this.requestExecutor = requireNonNull(requestExecutor);
    requestBuilder = MockMvcRequestBuilders.multipart(uri);
    requestBuilder.with(
        request -> {
          request.setMethod(httpMethod);
          return request;
        });
  }

  public TemplateMultipartRequest file(String name, byte[] content) {
    requestBuilder.file(name, content);
    return this;
  }

  public TemplateMultipartRequest file(MockMultipartFile file) {
    requestBuilder.file(file);
    return this;
  }

  public TemplateMultipartRequest part(Part... parts) {
    requestBuilder.part(parts);
    return this;
  }

  @Override
  public ResultActions submit() throws Exception {
    return requestExecutor.execute(requestBuilder);
  }

  @Override
  public HalMockMvc createAndShift() throws Exception {
    return requestExecutor.assertCreatedAndShift(submit());
  }

  @Override
  public HalMockMvc submitAndExpect204NoContent() throws Exception {
    return requestExecutor.assert204NoContentAndResume(submit());
  }
}
