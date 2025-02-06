package com.cosium.hal_mock_mvc;

import org.springframework.test.web.servlet.ResultActions;

/**
 * @author Réda Housni Alaoui
 */
public interface SubmittableTemplate {

  /** Submits the template */
  ResultActions submit() throws Exception;

  /**
   * Submits the template by expecting a 201 Created response then begins a new traversal starting
   * at the returned Location header.
   */
  HalMockMvc createAndShift() throws Exception;

  /** Submit the template by expecting 204 No Content then resume the traversal. */
  HalMockMvc submitAndExpect204NoContent() throws Exception;
}
