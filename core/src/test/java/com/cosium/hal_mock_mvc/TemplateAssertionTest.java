package com.cosium.hal_mock_mvc;

import static com.cosium.hal_mock_mvc.TemplateAsserts.hasTitle;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasMax;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasMaxLength;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasMin;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasMinLength;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasPrompt;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasReadOnly;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasRegex;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasRequired;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasStep;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasTemplated;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasType;
import static com.cosium.hal_mock_mvc.TemplatePropertyAsserts.hasValue;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import jakarta.inject.Inject;
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
@HalMockMvcBootTest
class TemplateAssertionTest {

  @Inject private MyController myController;
  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("Assert on template title")
  void test1() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .put("title", "Create")
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThat(hasTitle("Create"))).doesNotThrowAnyException();

    assertThatCode(() -> template.assertThat(it -> it.hasTitle("Foo")))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Template title expected:<Foo> but was:<Create>");
  }

  @Test
  @DisplayName("Assert on property prompt")
  void test2() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("prompt", "Foo")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasPrompt("Foo")))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasPrompt("Bar")))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property prompt expected:<Bar> but was:<Foo>");
  }

  @Test
  @DisplayName("Assert on property required")
  void test3() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("required", true)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasRequired(true)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasRequired(false)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property required expected:<false> but was:<true>");
  }

  @Test
  @DisplayName("Assert on property value")
  void test4() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("value", "my-value")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasValue("my-value")))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasValue("zzz")))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property value expected:<zzz> but was:<my-value>");
  }

  @Test
  @DisplayName("Assert on property readOnly")
  void test5() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("readOnly", true)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasReadOnly(true)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasReadOnly(false)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property readOnly expected:<false> but was:<true>");
  }

  @Test
  @DisplayName("Assert on property type")
  void test6() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "color")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasType("color")))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasType("text")))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property type expected:<text> but was:<color>");
  }

  @Test
  @DisplayName("Assert on property regex")
  void test7() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("regex", "abc")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasRegex("abc")))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasRegex("efg")))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property regex expected:<efg> but was:<abc>");
  }

  @Test
  @DisplayName("Assert on property templated")
  void test8() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("templated", true)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasTemplated(true)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasTemplated(false)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property templated expected:<false> but was:<true>");
  }

  @Test
  @DisplayName("Assert on property max")
  void test9() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("max", 5)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasMax(5D))).doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasMax(4D)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property max expected:<4.0> but was:<5.0>");
  }

  @Test
  @DisplayName("Assert on property min")
  void test10() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("min", 5)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasMin(5D))).doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasMin(4D)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property min expected:<4.0> but was:<5.0>");
  }

  @Test
  @DisplayName("Assert on property maxLength")
  void test11() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("maxLength", 5)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasMaxLength(5L)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasMaxLength(4L)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property maxLength expected:<4> but was:<5>");
  }

  @Test
  @DisplayName("Assert on property minLength")
  void test12() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("minLength", 5)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasMinLength(5L)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasMinLength(4L)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property minLength expected:<4> but was:<5>");
  }

  @Test
  @DisplayName("Assert on property step")
  void test13() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("step", 5)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasStep(5D)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasStep(4D)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property step expected:<4.0> but was:<5.0>");
  }

  @Test
  @DisplayName("Assert on non string property value")
  void test14() throws Exception {

    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/template-assertion-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("create")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("value", true)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    assertThatCode(() -> template.assertThatProperty("foo", hasValue(true)))
        .doesNotThrowAnyException();

    assertThatCode(() -> template.assertThatProperty("foo", it -> it.hasValue(false)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Property value expected:<false> but was:<true>");
  }

  @Controller
  public static class MyController {

    private String getResponseToSend;

    void reset() {
      getResponseToSend = null;
    }

    @GetMapping(value = "template-assertion-test:get", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(getResponseToSend);
    }

    @PutMapping("template-assertion-test:put")
    public ResponseEntity<?> put() {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
  }
}
