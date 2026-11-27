package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.inject.Inject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import tools.jackson.jr.ob.JSON;

/**
 * @author Réda Housni Alaoui
 */
@NullMarked
@HalMockMvcBootTest
class TemplatesTest {

  @Inject private MyController myController;
  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("Submit a template of a resource having a relation with several links")
  void test1() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", linkTo(methodOn(MyController.class).put()).toString())
            .end()
            .startArrayProperty("item")
            .startObject()
            .put("href", "https://example.org/items/1")
            .end()
            .startObject()
            .put("href", "https://example.org/items/2")
            .end()
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .end()
            .end()
            .end()
            .finish();

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .submit()
        .andExpect(status().isNotImplemented());
  }

  @Test
  @DisplayName("Submit a template of a resource having its self link in an array")
  void test2() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startArrayProperty("self")
            .startObject()
            .put("href", linkTo(methodOn(MyController.class).put()).toString())
            .end()
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .end()
            .end()
            .end()
            .finish();

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .submit()
        .andExpect(status().isNotImplemented());
  }

  @Test
  @DisplayName("Read the templates of a resource having two self links")
  void test3() {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startArrayProperty("self")
            .startObject()
            .put("href", linkTo(methodOn(MyController.class).put()).toString())
            .end()
            .startObject()
            .put("href", "https://example.org/other")
            .end()
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .end()
            .end()
            .end()
            .finish();

    HalMockMvc.TraversalBuilder traversal =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow();

    assertThatThrownBy(traversal::templates)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Resource has <2> self links instead of <1>");
  }

  @Controller
  public static class MyController {

    private @Nullable String getResponseToSend;

    void reset() {
      getResponseToSend = null;
    }

    @GetMapping(value = "templates-test:get", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(getResponseToSend);
    }

    @PutMapping("templates-test:put")
    public ResponseEntity<?> put() {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
  }
}
