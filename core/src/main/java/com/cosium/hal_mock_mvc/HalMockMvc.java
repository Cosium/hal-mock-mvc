package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.HalCompatibleContentTypeMatcher.contentTypeIsCompatibleWithHal;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 *
 *
 * <h3>HAL links browsing</h3>
 *
 * <pre>{@code
 * HalMockMvc.builder(mockMvc).baseUri(linkTo(methodOn(MyController.class).get()).toUri())
 *   .build()
 *   .follow("collection")
 *   .get()
 *   .andExpect(status().isOk())
 *   .andExpect(jsonPath("$._embedded.content.length()").value(1))
 *   .andExpect(jsonPath("$._embedded.content[0].name").value("foo"));
 * }</pre>
 *
 * <h3>HAL forms submission</h3>
 *
 * <pre>{@code
 * HalMockMvc.builder(mockMvc).baseUri(linkTo(methodOn(MyController.class).list()).toUri())
 * .build()
 * .follow()
 * .templates()
 * .byKey("create")
 * .submit(
 *   JSON.std
 *       .composeString()
 *       .startObject()
 *       .put("name", "john")
 *       .end()
 *       .finish()
 * )
 * .andExpect(status().isCreated());
 * }</pre>
 *
 * @author Réda Housni Alaoui
 */
public class HalMockMvc {

  public static final String DEFAULT_BASE_URI = "/";

  private final MockMvc mockMvc;
  private final String baseUri;
  private final List<RequestPostProcessor> requestPostProcessors;
  private final HttpHeaders headers;

  private HalMockMvc(
      MockMvc mockMvc,
      String baseUri,
      List<RequestPostProcessor> requestPostProcessors,
      HttpHeaders headers) {
    this.mockMvc = requireNonNull(mockMvc);
    this.baseUri = requireNonNull(baseUri);
    this.requestPostProcessors = List.copyOf(requestPostProcessors);
    this.headers = HttpHeaders.copyOf(headers);
  }

  public static Builder builder(MockMvc mockMvc) {
    return new Builder(mockMvc);
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public String baseUri() {
    return baseUri;
  }

  public TraversalBuilder follow(String... relations) {
    return new TraversalBuilder(mockMvc, baseUri, requestPostProcessors, new HttpHeaders(headers))
        .follow(relations);
  }

  public TraversalBuilder follow(Hop relation) {
    return new TraversalBuilder(mockMvc, baseUri, requestPostProcessors, new HttpHeaders(headers))
        .follow(relation);
  }

  /**
   * The traversal builder allows to navigate to a final endpoint through a sequence of relations
   */
  public static class TraversalBuilder {

    private final RequestExecutor requestExecutor;
    private final String baseUri;
    private final LinkDiscoverer halLinkDiscoverer = new HalFormsLinkDiscoverer();
    private final List<Hop> hops = new ArrayList<>();

    private TraversalBuilder(
        MockMvc mockMvc,
        String baseUri,
        List<RequestPostProcessor> postProcessors,
        HttpHeaders httpHeaders) {
      this.requestExecutor = new RequestExecutor(mockMvc, postProcessors, httpHeaders);
      this.baseUri = baseUri;
    }

    public TraversalBuilder follow(String... relation) {
      hops.addAll(Stream.of(relation).map(Hop::relation).toList());
      return this;
    }

    public TraversalBuilder follow(Hop relation) {
      hops.add(relation);
      return this;
    }

    /**
     * @return The HAL-FORMS templates provided by the final endpoint.
     */
    public Templates templates() throws Exception {
      return new Templates(requestExecutor, get());
    }

    /**
     * Begins the creation of a multipart request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#multipart(URI)
     */
    public MultipartRequest multipartRequest() throws Exception {
      return new MultipartRequest(requestExecutor, fetchTargetUri());
    }

    /**
     * Begins the creation of a request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#request(String, URI)
     */
    public Request request() throws Exception {
      return new Request(requestExecutor, fetchTargetUri());
    }

    /**
     * Begins the creation of a GET request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#get(URI)
     */
    public ResultActions get() throws Exception {
      return request().get();
    }

    /**
     * Begins the creation of a POST request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#post(URI)
     */
    public ResultActions post() throws Exception {
      return request().post();
    }

    /**
     * Begins the creation of a POST request with a JSON payload targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#post(URI)
     */
    public ResultActions post(String jsonContent) throws Exception {
      return request().jsonContent(jsonContent).post();
    }

    /**
     * Begins the creation of a PUT request with a JSON payload targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#put(URI)
     */
    public ResultActions put(String jsonContent) throws Exception {
      return request().jsonContent(jsonContent).put();
    }

    /**
     * Begins the creation of a PUT request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#put(URI)
     */
    public ResultActions put() throws Exception {
      return request().put();
    }

    /**
     * Begins the creation of a PATCH request with a JSON payload targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#patch(URI)
     */
    public ResultActions patch(String jsonContent) throws Exception {
      return request().jsonContent(jsonContent).patch();
    }

    /**
     * Begins the creation of a PATCH request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#patch(URI)
     */
    public ResultActions patch() throws Exception {
      return request().patch();
    }

    /**
     * Begins the creation of a DELETE request targeting the final endpoint
     *
     * @see MockMvcRequestBuilders#patch(URI)
     */
    public ResultActions delete() throws Exception {
      return request().delete();
    }

    private URI fetchTargetUri() throws Exception {
      URI targetUri = URI.create(baseUri);
      for (Hop hop : hops) {

        ResultActions requestResult =
            requestExecutor.execute(MockMvcRequestBuilders.get(targetUri));
        requestResult.andExpect(contentTypeIsCompatibleWithHal());

        MockHttpServletResponse response = requestResult.andReturn().getResponse();
        String body = response.getContentAsString();
        int responseStatus = response.getStatus();
        if (responseStatus < 200 || responseStatus >= 400) {
          throw new IllegalStateException(
              "GET on "
                  + targetUri
                  + " failed with code "
                  + responseStatus
                  + " and body '"
                  + body
                  + "'");
        }

        Link link = halLinkDiscoverer.findLinkWithRel(hop.relationName(), body).orElse(null);
        if (link == null) {
          throw new IllegalArgumentException(
              "Could not find relation "
                  + hop
                  + " at URI "
                  + requestResult.andReturn().getRequest().getRequestURI());
        }

        targetUri = link.expand(hop.parameters()).toUri();
      }

      return targetUri;
    }
  }

  public static class Builder {

    private final MockMvc mockMvc;
    private String baseUri;
    private final List<RequestPostProcessor> requestPostProcessors;
    private final HttpHeaders headers;

    private Builder(MockMvc mockMvc) {
      this(mockMvc, DEFAULT_BASE_URI, List.of(), new HttpHeaders());
    }

    private Builder(HalMockMvc halMockMvc) {
      this(
          halMockMvc.mockMvc,
          halMockMvc.baseUri,
          halMockMvc.requestPostProcessors,
          halMockMvc.headers);
    }

    private Builder(
        MockMvc mockMvc,
        String baseUri,
        List<RequestPostProcessor> requestPostProcessors,
        HttpHeaders headers) {
      this.mockMvc = mockMvc;
      this.baseUri = baseUri;
      this.requestPostProcessors = new ArrayList<>(requestPostProcessors);
      this.headers = HttpHeaders.copyOf(headers);
    }

    /**
     * @param baseUri The URI from which the traversal will begin
     */
    public Builder baseUri(String baseUri) {
      this.baseUri = baseUri;
      return this;
    }

    /**
     * @param baseUri The URI from which the traversal will begin
     */
    public Builder baseUri(URI baseUri) {
      this.baseUri = baseUri.toString();
      return this;
    }

    public Builder addRequestPostProcessor(RequestPostProcessor requestPostProcessor) {
      requestPostProcessors.add(requestPostProcessor);
      return this;
    }

    public Builder requestPostProcessors(List<RequestPostProcessor> requestPostProcessors) {
      this.requestPostProcessors.clear();
      this.requestPostProcessors.addAll(requestPostProcessors);
      return this;
    }

    /**
     * @param name The header name
     * @param values The header values
     */
    public Builder header(String name, String... values) {
      headers.put(name, List.of(values));
      return this;
    }

    public Builder headers(HttpHeaders headers) {
      this.headers.clear();
      this.headers.addAll(headers);
      return this;
    }

    public HalMockMvc build() {
      return new HalMockMvc(mockMvc, baseUri, requestPostProcessors, headers);
    }
  }
}
