package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * @author Réda Housni Alaoui
 */
class RequestExecutor {

  private final MockMvc mockMvc;
  private final List<RequestPostProcessor> postProcessors;
  private final HttpHeaders httpHeaders;

  public RequestExecutor(
      MockMvc mockMvc, List<RequestPostProcessor> postProcessors, HttpHeaders httpHeaders) {
    this.mockMvc = requireNonNull(mockMvc);
    this.postProcessors = List.copyOf(postProcessors);
    this.httpHeaders = requireNonNull(httpHeaders);
  }

  public ResultActions execute(MockHttpServletRequestBuilder requestBuilder) throws Exception {
    postProcessors.forEach(requestBuilder::with);
    return mockMvc.perform(requestBuilder.accept(MediaTypes.HAL_FORMS_JSON).headers(httpHeaders));
  }

  public HalMockMvc assertCreatedAndShift(ResultActions resultActions) throws Exception {
    String location =
        resultActions
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");

    requireNonNull(location, "No header 'Location' found");

    return shiftTo(location);
  }

  private HalMockMvc shiftTo(String location) {
    return HalMockMvc.builder(mockMvc)
        .baseUri(location)
        .requestPostProcessors(postProcessors)
        .headers(httpHeaders)
        .build();
  }
}
