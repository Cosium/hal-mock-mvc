package com.cosium.hal_mock_mvc_spring_boot_starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.cosium.hal_mock_mvc.HalMockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Réda Housni Alaoui
 */
class DefaultHalMockMvcBuilderCustomizerTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(HalMockMvcAutoConfiguration.class));

  @Test
  @DisplayName("Customizer is not required")
  void test1() {
    contextRunner
        .withUserConfiguration(MockMvcConfiguration.class)
        .run(
            context ->
                assertThat(context.getBean(HalMockMvc.class).baseUri())
                    .isEqualTo(HalMockMvc.DEFAULT_BASE_URI));
  }

  @Test
  @DisplayName("When a customizer is present, it is used")
  void test2() {
    contextRunner
        .withUserConfiguration(MockMvcConfiguration.class, CustomizerConfiguration.class)
        .run(context -> assertThat(context.getBean(HalMockMvc.class).baseUri()).isEqualTo("/foo"));
  }

  static class CustomizerConfiguration {

    @Bean
    DefaultHalMockMvcBuilderCustomizer customizer() {
      return builder -> builder.baseUri("/foo");
    }
  }

  static class MockMvcConfiguration {
    @Bean
    MockMvc mockMvc() {
      return MockMvcBuilders.standaloneSetup().build();
    }
  }
}
