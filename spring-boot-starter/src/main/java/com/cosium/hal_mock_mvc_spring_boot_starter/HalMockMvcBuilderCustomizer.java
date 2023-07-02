package com.cosium.hal_mock_mvc_spring_boot_starter;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;

/**
 * A customizer for a {@link com.cosium.hal_mock_mvc.HalMockMvc.Builder} . Any {@link
 * HalMockMvcBuilderCustomizer} beans found in the application context will be used to initialize
 * every {@link com.cosium.hal_mock_mvc.HalMockMvc.Builder} produced by {@link HalMockMvcBuilders}.
 *
 * @author Réda Housni Alaoui
 */
@FunctionalInterface
public interface HalMockMvcBuilderCustomizer {
  void customize(HalMockMvc.Builder builder);
}
