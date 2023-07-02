package com.cosium.hal_mock_mvc_spring_boot_starter;

import com.cosium.hal_mock_mvc.HalMockMvc;

/**
 * A customizer for a {@link com.cosium.hal_mock_mvc.HalMockMvc.Builder} . Any {@link
 * DefaultHalMockMvcBuilderCustomizer} beans found in the application context will be used to
 * customize the autoconfigured {@link com.cosium.hal_mock_mvc.HalMockMvc.Builder}.
 *
 * @author Réda Housni Alaoui
 * @see HalMockMvcAutoConfiguration
 */
@FunctionalInterface
public interface DefaultHalMockMvcBuilderCustomizer {
  void customize(HalMockMvc.Builder builder);
}
