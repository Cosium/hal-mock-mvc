package com.cosium.hal_mock_mvc_spring_boot_starter;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilderFactory;
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
  public HalMockMvcBuilderFactory halMockMvcBuilderFactory(MockMvc mockMvc) {
    return new HalMockMvcBuilderFactory(mockMvc);
  }

  @ConditionalOnMissingBean
  @Bean
  public HalMockMvc defaultHalMockMvc(
      MockMvc mockMvc, @Nullable List<DefaultHalMockMvcBuilderCustomizer> nullableCustomizers) {

    HalMockMvc.Builder builder = HalMockMvc.builder(mockMvc);

    Optional.ofNullable(nullableCustomizers)
        .orElseGet(List::of)
        .forEach(customizer -> customizer.customize(builder));

    return builder.build();
  }
}
