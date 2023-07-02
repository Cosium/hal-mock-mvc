package com.cosium.hal_mock_mvc_spring_boot_starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilderFactory;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Réda Housni Alaoui
 */
class DefaultBacksOffTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(HalMockMvcAutoConfiguration.class));

  @Test
  @DisplayName("Default halMockMvcBuilderFactory backs off")
  void test1() {
    contextRunner
        .withUserConfiguration(UserConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(HalMockMvcBuilders.class);
              assertThat(context)
                  .getBean("myBuilders")
                  .isSameAs(context.getBean(HalMockMvcBuilders.class));
            });
  }

  @Test
  @DisplayName("Default defaultHalMockMvc backs off")
  void test2() {
    contextRunner
        .withUserConfiguration(UserConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(HalMockMvc.class);
              assertThat(context)
                  .getBean("myDefaultHalMockMvc")
                  .isSameAs(context.getBean(HalMockMvc.class));
            });
  }

  static class UserConfiguration {

    @Bean
    HalMockMvcBuilders myBuilders() {
      return new HalMockMvcBuilderFactory(MockMvcBuilders.standaloneSetup().build());
    }

    @Bean
    HalMockMvc myDefaultHalMockMvc() {
      return new HalMockMvcBuilderFactory(MockMvcBuilders.standaloneSetup().build())
          .create()
          .build();
    }
  }
}
