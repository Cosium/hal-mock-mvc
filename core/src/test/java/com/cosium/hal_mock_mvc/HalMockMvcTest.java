package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Réda Housni Alaoui
 */
@HalMockMvcBootTest
class HalMockMvcTest {

  @Inject private WebApplicationContext webApplicationContext;
  @Inject private MyController myController;

  private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    myController.reset();
  }

  @Test
  @DisplayName("HalMockMvc base uri should default to server base path")
  void test1() throws Exception {
    HalMockMvc.builder(mockMvc)
        .build()
        .follow()
        .get()
        .andExpect(status().isOk())
        .andExpect(IndexController.matchRootResource());
  }

  @Test
  @DisplayName("HalMockMvc produces HAL representation")
  void test2() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow("collection")
        .get()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.singletonMapList.length()").value(1))
        .andExpect(jsonPath("$._embedded.singletonMapList[0].name").value("foo"));
  }

  @Test
  @DisplayName("Base uri query string is preserved")
  void test3() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).delete("yo")).toUri())
        .build()
        .follow()
        .delete()
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE on target uri should not lead to a GET on this same target URI")
  void test4() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).delete("yo")).toUri())
        .build()
        .follow()
        .delete()
        .andExpect(status().isNoContent());

    assertThat(myController.getDeleteCalled).isFalse();
  }

  @Test
  @DisplayName("PUT multipart")
  void test5() throws Exception {
    byte[] fileContent = "yo".getBytes(StandardCharsets.UTF_8);
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).putMultipart("foo", null)).toUri())
        .build()
        .follow()
        .multipartRequest()
        .file("file", fileContent)
        .put()
        .andExpect(status().isNoContent());
    assertThat(myController.fileById.get("foo").getBytes()).isEqualTo(fileContent);
  }

  @Test
  @DisplayName("RequestPostProcessors are considered")
  void test6() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).getYoHeaderValue(null)).toUri())
        .addRequestPostProcessor(
            request -> {
              request.addHeader("yo", "man");
              return request;
            })
        .build()
        .follow()
        .get()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value("man"));
  }

  @Controller
  @RequestMapping("/HalMockMvcTest")
  public static class MyController {

    private final AtomicBoolean getDeleteCalled = new AtomicBoolean();
    private final Map<String, MultipartFile> fileById = new HashMap<>();

    private void reset() {
      getDeleteCalled.set(false);
      fileById.clear();
    }

    @GetMapping
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(
          new RepresentationModel<>(
              List.of(linkTo(methodOn(MyController.class).getCollection()).withRel("collection"))));
    }

    @GetMapping("/collection")
    public ResponseEntity<?> getCollection() {
      return ResponseEntity.ok(
          CollectionModel.of(Collections.singleton(Collections.singletonMap("name", "foo"))));
    }

    @GetMapping("/delete")
    public ResponseEntity<?> getDelete() {
      getDeleteCalled.set(true);
      return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam("name") String name) {
      return ResponseEntity.noContent().build();
    }

    @PutMapping("/multipart/{id}")
    public ResponseEntity<?> putMultipart(
        @PathVariable("id") String id, @RequestParam("file") MultipartFile file) {
      fileById.put(id, file);
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/yo-header")
    public ResponseEntity<?> getYoHeaderValue(@RequestHeader("yo") String yoValue) {
      return ResponseEntity.ok(Map.of("value", yoValue));
    }
  }
}
