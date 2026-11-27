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
class EmbeddedResourceTest {

  @Inject private MyController myController;
  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("List the resources of an embedded array with their templates")
  void test1() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {
            "content": [
              {
                "_links": {"self": {"href": "http://localhost/embedded-resource-test:item"}},
                "_templates": {"update": {"method": "PUT"}}
              },
              {
                "_links": {"self": {"href": "http://localhost/embedded-resource-test:other-item"}},
                "_templates": {"delete": {"method": "DELETE"}}
              }
            ]
          }
        }
        """;

    List<EmbeddedResource> embeddedResources =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .embeddedList("content");

    assertThat(embeddedResources)
        .extracting(EmbeddedResource::selfHref)
        .containsExactly(
            "http://localhost/embedded-resource-test:item",
            "http://localhost/embedded-resource-test:other-item");
    Templates firstTemplates = embeddedResources.get(0).templates();
    firstTemplates.byKey("update").assertThat(hasMethod("PUT"));
    assertThat(firstTemplates.byOptionalKey("delete")).isEmpty();
    Templates secondTemplates = embeddedResources.get(1).templates();
    secondTemplates.byKey("delete").assertThat(hasMethod("DELETE"));
    assertThat(secondTemplates.byOptionalKey("update")).isEmpty();
  }

  @Test
  @DisplayName("Get a single embedded resource with its templates")
  void test2() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-resource-test:item"}},
              "_templates": {"update": {"method": "PUT"}}
            }
          }
        }
        """;

    EmbeddedResource embeddedResource =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .embedded("owner");

    assertThat(embeddedResource.selfHref())
        .isEqualTo("http://localhost/embedded-resource-test:item");
    embeddedResource.templates().byKey("update").assertThat(hasMethod("PUT"));
  }

  @Test
  @DisplayName("Submit an embedded template without target to the embedded resource self link")
  void test3() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-resource-test:item"}},
              "_templates": {"update": {"method": "PUT"}}
            }
          }
        }
        """;

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .embedded("owner")
        .templates()
        .byKey("update")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(myController.itemPutCount).isEqualTo(1);
  }

  @Test
  @DisplayName("Get a single embedded resource from an embedded array")
  void test4() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {
            "content": [
              {
                "_links": {"self": {"href": "http://localhost/embedded-resource-test:item"}}
              }
            ]
          }
        }
        """;

    HalMockMvc.TraversalBuilder traversal =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow();

    assertThatThrownBy(() -> traversal.embedded("content"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Embedded relation <content> is <ARRAY> instead of <OBJECT>");
  }

  @Test
  @DisplayName("List the resources of an embedded array from a single embedded resource")
  void test5() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {
            "owner": {
              "_links": {"self": {"href": "http://localhost/embedded-resource-test:item"}}
            }
          }
        }
        """;

    HalMockMvc.TraversalBuilder traversal =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow();

    assertThatThrownBy(() -> traversal.embeddedList("owner"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Embedded relation <owner> is <OBJECT> instead of <ARRAY>");
  }

  @Test
  @DisplayName("Get a resource from a missing embedded relation")
  void test6() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}}
        }
        """;

    HalMockMvc.TraversalBuilder traversal =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow();

    assertThatThrownBy(() -> traversal.embedded("owner"))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("No embedded resource found for relation <owner>");
  }

  @Test
  @DisplayName("Get the self href of an embedded resource without self link")
  void test7() throws Exception {

    myController.getResponseToSend =
        """
        {
          "_links": {"self": {"href": "http://localhost/embedded-resource-test:get"}},
          "_embedded": {"owner": {"name": "John"}}
        }
        """;

    EmbeddedResource embeddedResource =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .embedded("owner");

    assertThatThrownBy(embeddedResource::selfHref)
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("_links.self.href is missing from the embedded resource");
  }

  @Controller
  public static class MyController {

    private String getResponseToSend;
    private int itemPutCount;

    void reset() {
      getResponseToSend = null;
      itemPutCount = 0;
    }

    @GetMapping(value = "embedded-resource-test:get", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(getResponseToSend);
    }

    @PutMapping("embedded-resource-test:item")
    public ResponseEntity<?> putItem() {
      itemPutCount++;
      return ResponseEntity.noContent().build();
    }
  }
}
