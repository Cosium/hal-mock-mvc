package com.cosium.hal_mock_mvc_spring_boot_starter;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilderFactory;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Auto-configuration for {@link com.cosium.hal_mock_mvc.HalMockMvc}.
 *
 * @author Réda Housni Alaoui
 * @see AutoConfigureHalMockMvc
 */
@AutoConfiguration(after = MockMvcAutoConfiguration.class)
@ConditionalOnBean(MockMvc.class)
public class HalMockMvcAutoConfiguration {

  @ConditionalOnMissingBean
  @Bean
  public HalMockMvcBuilders halMockMvcBuilderFactory(
      MockMvc mockMvc, @Nullable List<HalMockMvcBuilderCustomizer> customizers) {
    return new CustomizableHalMockMvcBuilders(new HalMockMvcBuilderFactory(mockMvc), customizers);
  }

  @ConditionalOnMissingBean
  @Bean
  public HalMockMvc defaultHalMockMvc(
      HalMockMvcBuilders halMockMvcBuilders,
      @Nullable List<DefaultHalMockMvcBuilderCustomizer> nullableCustomizers) {

    HalMockMvc.Builder builder = halMockMvcBuilders.create();

    Optional.ofNullable(nullableCustomizers)
        .orElseGet(List::of)
        .forEach(customizer -> customizer.customize(builder));

    return builder.build();
  }
}
