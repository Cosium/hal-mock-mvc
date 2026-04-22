package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * @author Réda Housni Alaoui
 */
class RequestExecutor {

  private final MockMvc mockMvc;
  private final List<RequestPostProcessor> postProcessors;
  private final List<RelationsRequestPostProcessor> relationsRequestPostProcessors;
  private final HttpHeaders httpHeaders;

  public RequestExecutor(
      MockMvc mockMvc,
      List<RequestPostProcessor> postProcessors,
      List<RelationsRequestPostProcessor> relationsRequestPostProcessors,
      HttpHeaders httpHeaders) {
    this.mockMvc = requireNonNull(mockMvc);
    this.postProcessors = List.copyOf(postProcessors);
    this.relationsRequestPostProcessors = requireNonNull(relationsRequestPostProcessors);
    this.httpHeaders = requireNonNull(httpHeaders);
  }

  public ResultActions fetchRelations(URI targetUri, String... desiredRelations) throws Exception {
    List<RequestPostProcessor> requestPostProcessors =
        Stream.concat(
                postProcessors.stream(),
                relationsRequestPostProcessors.stream()
                    .map(toRequestPostProcessor(desiredRelations)))
            .toList();

    return doExecute(MockMvcRequestBuilders.get(targetUri), requestPostProcessors);
  }

  public ResultActions execute(AbstractMockHttpServletRequestBuilder<?> requestBuilder)
      throws Exception {

    return doExecute(requestBuilder, postProcessors);
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

  public HalMockMvc assert204NoContentAndResume(ResultActions resultActions) throws Exception {
    resultActions.andExpect(status().isNoContent());
    String requestURI = resultActions.andReturn().getRequest().getRequestURI();
    return shiftTo(requestURI);
  }

  private ResultActions doExecute(
      AbstractMockHttpServletRequestBuilder<?> requestBuilder,
      List<RequestPostProcessor> requestPostProcessors)
      throws Exception {
    requestPostProcessors.forEach(requestBuilder::with);
    return mockMvc.perform(requestBuilder.accept(MediaTypes.HAL_FORMS_JSON).headers(httpHeaders));
  }

  private HalMockMvc shiftTo(String location) {
    return HalMockMvc.builder(mockMvc)
        .baseUri(location)
        .requestPostProcessors(postProcessors)
        .headers(httpHeaders)
        .build();
  }

  private Function<RelationsRequestPostProcessor, RequestPostProcessor> toRequestPostProcessor(
      String... desiredRelations) {
    return relationsRequestPostProcessor ->
        request ->
            relationsRequestPostProcessor.postProcessRequest(request, Set.of(desiredRelations));
  }
}
