package com.cosium.hal_mock_mvc_spring_boot_starter;

import com.cosium.hal_mock_mvc.HalMockMvcBuilderFactory;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;
import java.util.List;
import java.util.Optional;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Réda Housni Alaoui
 */
public class HalMockMvcBuildersFactory {

  private final List<HalMockMvcBuilderCustomizer> customizers;

  public HalMockMvcBuildersFactory(List<HalMockMvcBuilderCustomizer> customizers) {
    this.customizers = Optional.ofNullable(customizers).map(List::copyOf).orElseGet(List::of);
  }

  public HalMockMvcBuilders build(MockMvc mockMvc) {
    return new CustomizableHalMockMvcBuilders(new HalMockMvcBuilderFactory(mockMvc), customizers);
  }
}
