package com.cosium.hal_mock_mvc;

import java.util.Set;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Réda Housni Alaoui
 */
@FunctionalInterface
public interface RelationsRequestPostProcessor {

  /**
   * Post-process a request whose purpose is to retrieve a collection of relations.
   *
   * @param request The request to post-process
   * @param desiredRelations The relations looked up for
   * @return The post-processed request
   */
  MockHttpServletRequest postProcessRequest(
      MockHttpServletRequest request, Set<String> desiredRelations);
}
