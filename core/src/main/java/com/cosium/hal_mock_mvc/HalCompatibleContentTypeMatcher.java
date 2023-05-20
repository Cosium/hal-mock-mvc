package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.OrMatcher.anyOf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * @author Réda Housni Alaoui
 */
class HalCompatibleContentTypeMatcher implements ResultMatcher {

  private HalCompatibleContentTypeMatcher() {}

  public static ResultMatcher contentTypeIsCompatibleWithHal() {
    return new HalCompatibleContentTypeMatcher();
  }

  @Override
  public void match(MvcResult result) throws Exception {
    anyOf(
            content().contentType(MediaTypes.HAL_FORMS_JSON),
            content().contentType(MediaTypes.HAL_JSON))
        .match(result);
  }
}
