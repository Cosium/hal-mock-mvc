package com.cosium.hal_mock_mvc_spring_boot_starter;

import static org.assertj.core.api.Assertions.assertThat;

import com.cosium.hal_mock_mvc.HalMockMvc;
import com.cosium.hal_mock_mvc.HalMockMvcBuilders;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Réda Housni Alaoui
 */
@SpringBootTest
@AutoConfigureHalMockMvc
class AutoConfigureHalMockMvcTest {

  @Inject private HalMockMvcBuilders halMockMvcBuilders;
  @Inject private HalMockMvc halMockMvc;

  @Test
  void test() {
    assertThat(halMockMvcBuilders).isNotNull();
    assertThat(halMockMvc).isNotNull();
  }
}
