package com.cosium.hal_mock_mvc_spring_boot_starter;

import static java.util.Objects.requireNonNull;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;
import java.util.List;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
class CustomizableHalMockMvcBuilders implements HalMockMvcBuilders {

  private final HalMockMvcBuilders delegate;
  private final List<HalMockMvcBuilderCustomizer> customizers;

  public CustomizableHalMockMvcBuilders(
      HalMockMvcBuilders delegate, List<HalMockMvcBuilderCustomizer> customizers) {
    this.delegate = requireNonNull(delegate);
    this.customizers = Optional.ofNullable(customizers).map(List::copyOf).orElseGet(List::of);
  }

  @Override
  public HalMockMvc.Builder create() {
    HalMockMvc.Builder builder = delegate.create();
    customizers.forEach(customizer -> customizer.customize(builder));
    return builder;
  }
}
