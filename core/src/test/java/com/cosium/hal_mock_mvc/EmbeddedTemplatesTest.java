package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.TemplateAsserts.hasMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author Réda Housni Alaoui
 */
@HalMockMvcBootTest
class EmbeddedTemplatesTest {

  @Inject private MyController myController;
  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("List the templates of each resource of an embedded array")
  void test1() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}},
          "_embedded": {
            "content": [
              {
                "_links": {"self": {"href": "http://localhost/embedded-templates-test:item"}},
                "_templates": {"update": {"method": "PUT"}}
              },
              {
                "_links": {"self": {"href": "http://localhost/embedded-templates-test:other-item"}},
                "_templates": {"delete": {"method": "DELETE"}}
              }
            ]
          }
        }
        """;

    List<Templates> embeddedTemplates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .embeddedList("content");

    assertThat(embeddedTemplates)
        .extracting(Templates::selfHref)
        .containsExactly(
            "http://localhost/embedded-templates-test:item",
            "http://localhost/embedded-templates-test:other-item");
    embeddedTemplates.get(0).byKey("update").assertThat(hasMethod("PUT"));
    assertThat(embeddedTemplates.get(0).byOptionalKey("delete")).isEmpty();
    embeddedTemplates.get(1).byKey("delete").assertThat(hasMethod("DELETE"));
    assertThat(embeddedTemplates.get(1).byOptionalKey("update")).isEmpty();
  }

  @Test
  @DisplayName("Get the templates of a single embedded resource")
  void test2() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-templates-test:item"}},
              "_templates": {"update": {"method": "PUT"}}
            }
          }
        }
        """;

    Templates embeddedTemplates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .embedded("owner");

    assertThat(embeddedTemplates.selfHref())
        .isEqualTo("http://localhost/embedded-templates-test:item");
    embeddedTemplates.byKey("update").assertThat(hasMethod("PUT"));
  }

  @Test
  @DisplayName("Submit an embedded template without target to the embedded resource self link")
  void test3() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-templates-test:item"}},
              "_templates": {"update": {"method": "PUT"}}
            }
          }
        }
        """;

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .embedded("owner")
        .byKey("update")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(myController.itemPutCount).isEqualTo(1);
  }

  @Test
  @DisplayName("Get the templates of a single embedded resource from an embedded array")
  void test4() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}},
          "_embedded": {
            "content": [
              {
                "_links": {"self": {"href": "http://localhost/embedded-templates-test:item"}}
              }
            ]
          }
        }
        """;

    Templates templates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates();

    assertThatThrownBy(() -> templates.embedded("content"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Embedded relation <content> is <ARRAY> instead of <OBJECT>");
  }

  @Test
  @DisplayName("List the templates of an embedded array from a single embedded resource")
  void test5() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-templates-test:item"}}
            }
          }
        }
        """;

    Templates templates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates();

    assertThatThrownBy(() -> templates.embeddedList("owner"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Embedded relation <owner> is <OBJECT> instead of <ARRAY>");
  }

  @Test
  @DisplayName("Get the templates of a missing embedded relation")
  void test6() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-templates-test:get"}}
        }
        """;

    Templates templates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates();

    assertThatThrownBy(() -> templates.embedded("owner"))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("No embedded resource found for relation <owner>");
  }

  @Controller
  public static class MyController {

    private String getResponseToSend;
    private int itemPutCount;

    void reset() {
      getResponseToSend = null;
      itemPutCount = 0;
    }

    @GetMapping(value = "embedded-templates-test:get", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(getResponseToSend);
    }

    @PutMapping("embedded-templates-test:item")
    public ResponseEntity<?> putItem() {
      itemPutCount++;
      return ResponseEntity.noContent().build();
    }
  }
}
