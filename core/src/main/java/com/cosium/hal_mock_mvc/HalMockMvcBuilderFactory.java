package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Réda Housni Alaoui
 */
public class HalMockMvcBuilderFactory {

  private final MockMvc mockMvc;

  public HalMockMvcBuilderFactory(MockMvc mockMvc) {
    this.mockMvc = requireNonNull(mockMvc);
  }

  public HalMockMvc.Builder create() {
    return HalMockMvc.builder(mockMvc);
  }
}
