package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cosium.hal_mock_mvc.PropertyValidationOption.Immediate;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.jr.ob.JSON;

/**
 * @author Réda Housni Alaoui
 */
@HalMockMvcBootTest
class FormTest {

  @Inject private MyController myController;
  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("User cannot pass an unknown property")
  void test1() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();

    assertThatThrownBy(() -> form.withString("foo", "foo"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("No property 'foo' found.");
  }

  @Test
  @DisplayName("User cannot pass a value to a readonly property")
  void test2() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();
    assertThatThrownBy(() -> form.withString("foo", "foo"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Cannot set value for read-only property 'foo'");
  }

  @Test
  @DisplayName("User cannot pass a an array property without options")
  void test3() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withStrings("foo", List.of("value"));

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Given property 'foo' has no options attribute, it cannot hold an array of values.]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "hidden",
        "text",
        "textarea",
        "search",
        "tel",
        "url",
        "email",
        "password",
        "date",
        "time",
        "datetime-local",
        "range",
        "color"
      })
  @DisplayName("User cannot pass a non string value when the type is only compatible with text")
  void test4(String halFormType) throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", halFormType)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withBoolean("foo", true);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value must be of type String because property 'foo' has type '%s']. Got http status code 204 instead."
                .formatted(halFormType));

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "hidden",
        "text",
        "textarea",
        "search",
        "tel",
        "url",
        "email",
        "password",
        "date",
        "time",
        "datetime-local",
        "range",
        "color"
      })
  @DisplayName("User can pass a string value when the type is only compatible with text")
  void test5(String halFormType) throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", halFormType)
            .end()
            .end()
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
        .createForm()
        .withString("foo", "bar")
        .submit()
        .andExpect(status().isNoContent());

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo", String.class)).isEqualTo("bar");
  }

  @ParameterizedTest
  @ValueSource(strings = {"month", "week", "number"})
  @DisplayName("User cannot pass a non number value when the type is only compatible with number")
  void test6(String halFormType) throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", halFormType)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withBoolean("foo", true);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value must be of type Number because property 'foo' has type '%s']. Got http status code 204 instead."
                .formatted(halFormType));

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(strings = {"month", "week", "number"})
  @DisplayName("User can pass a number value when the type is only compatible with number")
  void test7(String halFormType) throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", halFormType)
            .end()
            .end()
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
        .createForm()
        .withInteger("foo", 1)
        .submit()
        .andExpect(status().isNoContent());

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo", Integer.class)).isEqualTo(1);
  }

  @Test
  @DisplayName("User cannot pass a value not matching the required regex")
  void test8() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("regex", "foo.+")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "foo");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value 'foo' of property 'foo' does not match regex 'foo.+']. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a value matching the required regex")
  void test9() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("regex", "foo.+")
            .end()
            .end()
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
        .createForm()
        .withString("foo", "foobar")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("foobar");
  }

  @Test
  @DisplayName("User cannot pass a non string value when a regex validation is needed")
  void test10() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("regex", "foo.+")
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withLong("foo", 22L);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Property 'foo' must have a value of type String because it is associated to a regex]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User cannot pass a number greater than the max value")
  void test11() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("max", 11.6)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withDouble("foo", 12.4d);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value 12.4 is greater than the max value of 11.6 of property 'foo'.]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a number lower than the max value")
  void test12() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("max", 11.6)
            .end()
            .end()
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
        .createForm()
        .withDouble("foo", 11.6)
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", Double.class))
        .isEqualTo(11.6d);
  }

  @Test
  @DisplayName("User cannot pass a number lower than the min value")
  void test13() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("min", 11.6)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withInteger("foo", 11);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value 11.0 is lower than the min value of 11.6 of property 'foo'.]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a number greater than the min value")
  void test14() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("min", 11.6)
            .end()
            .end()
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
        .createForm()
        .withInteger("foo", 12)
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", Integer.class))
        .isEqualTo(12);
  }

  @Test
  @DisplayName("User cannot pass a string having a length greater than the max length")
  void test15() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "123456");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value '123456' of property 'foo' has a greater length than the defined max length of 5]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a string having a length lower than the max length")
  void test16() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .createForm()
        .withString("foo", "1234")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("1234");
  }

  @Test
  @DisplayName("User cannot pass a string having a length lower than the min length")
  void test17() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "1234");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value '1234' of property 'foo' has a lower length than the defined min length of 5]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a string having a length greater than the min length")
  void test18() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .createForm()
        .withString("foo", "123456")
        .submit();

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("123456");
  }

  @Test
  @DisplayName("User cannot pass a number that is not compatible with the required step")
  void test19() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("step", 0.3)
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withDouble("foo", 0.85);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value '0.85' of property 'foo' is not compatible with the defined step of '0.3']. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can pass a number that is compatible with the required step")
  void test20() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("type", "number")
            .put("step", 0.3)
            .end()
            .end()
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
        .createForm()
        .withDouble("foo", 1.5)
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", Double.class))
        .isEqualTo(1.5d);
  }

  @Test
  @DisplayName("Default value is implicitly submitted")
  void test21() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("value", "bar")
            .put("required", true)
            .end()
            .end()
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
        .createForm()
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("bar");
  }

  @Test
  @DisplayName("User cannot submit a form missing a required property")
  void test22() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Property 'foo' is required but is missing]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can submit a form with non missing required property")
  void test23() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .createForm()
        .withString("foo", "bar")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("bar");
  }

  @Test
  @DisplayName("User cannot submit a form having a required property valued to null")
  void test24() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", null);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Since property 'foo' is required, it must hold at least one non-null value.,Property 'foo' is required but is missing]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
      "User cannot submit an option constrained property without a way to retrieve those options")
  void test25() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();

    assertThatThrownBy(() -> form.withString("foo", "bar"))
        .isInstanceOf(AssertionError.class)
        .hasMessage("Missing options inline and remote elements for property 'foo'.");
  }

  @Test
  @DisplayName(
      "User cannot submit an option constrained property with a type different from String")
  void test26() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .startArrayProperty("inline")
            .startObject()
            .put("value", 1)
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withInteger("foo", 1);

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value of type 'class java.lang.Integer' is not valid because the type should be of type 'String' given property 'foo' expects a value from an enumeration of options.]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("User can submit an option constrained property with a valid value")
  void test27() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("valueField", "theValue")
            .startArrayProperty("inline")
            .startObject()
            .put("theValue", "bar")
            .end()
            .end()
            .end()
            .end()
            .end()
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
        .createForm()
        .withString("foo", "bar")
        .submit();

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("bar");
  }

  @Test
  @DisplayName("User cannot submit an option constrained property with a invalid value")
  void test28() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "baz");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value 'baz' didn't match any inline option of property 'foo' among [MapInlineElementRepresentation[map={value=bar}]]]. Got http status code 204 instead.");
  }

  @Test
  @DisplayName(
      "User cannot submit an option constrained property with more values than authorized by maxItems")
  void test29() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("maxItems", 1)
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .startObject()
            .put("value", "baz")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withStrings("foo", List.of("bar", "baz"));

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [2 values passed for property 'foo' while maxItems == 1]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
      "User can submit an option constrained property with less values than authorized by maxItems")
  void test30() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("maxItems", 3)
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .startObject()
            .put("value", "baz")
            .end()
            .end()
            .end()
            .end()
            .end()
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
        .createForm()
        .withStrings("foo", List.of("bar", "baz"))
        .submit();

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo[0]", String.class)).isEqualTo("bar");
    assertThat(submittedDocument.read("$.foo[1]", String.class)).isEqualTo("baz");
  }

  @Test
  @DisplayName(
      "User cannot submit an option constrained property with less values than authorized by minItems")
  void test31() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("minItems", 2)
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .startObject()
            .put("value", "baz")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "bar");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [1 values passed for property 'foo' while minItems == 2]. Got http status code 204 instead.");

    myController.putResponseToSend = ResponseEntity.badRequest().build();
    form.submit().andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName(
      "User can submit an option constrained property with more values than authorized by minItems")
  void test32() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("minItems", 1)
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .startObject()
            .put("value", "baz")
            .end()
            .end()
            .end()
            .end()
            .end()
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
        .createForm()
        .withStrings("foo", List.of("bar", "baz"))
        .submit();

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo[0]", String.class)).isEqualTo("bar");
    assertThat(submittedDocument.read("$.foo[1]", String.class)).isEqualTo("baz");
  }

  @Test
  @DisplayName("User cannot submit a remote option constrained property with a invalid value")
  void test33() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .startObjectProperty("link")
            .put("href", "http://localhost/form-test:get-options")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    myController.getOptionsResponseToSend =
        ResponseEntity.ok(
            JSON.std
                .composeString()
                .startArray()
                .startObject()
                .put("value", "bar")
                .end()
                .end()
                .finish());

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm()
            .withString("foo", "baz");

    assertThatThrownBy(form::submit)
        .isInstanceOf(AssertionError.class)
        .hasMessage(
            "An http status code 400 was expected because of the following reasons: [Value 'baz' didn't match any inline option of property 'foo' among [MapInlineElementRepresentation[map={value=bar}]]]. Got http status code 204 instead.");
  }

  @Test
  @DisplayName("User can submit a remote option constrained property with a valid value")
  void test34() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .startObjectProperty("link")
            .put("href", "http://localhost/form-test:get-options")
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .end()
            .finish();

    myController.getOptionsResponseToSend =
        ResponseEntity.ok(
            JSON.std
                .composeString()
                .startArray()
                .startObject()
                .put("value", "bar")
                .end()
                .end()
                .finish());

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("default")
        .createForm()
        .withString("foo", "bar")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("bar");
  }

  @Test
  @DisplayName("Pre selected options with maxItems > 1 are implicitly submitted as array")
  void test35() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .startArrayProperty("selectedValues")
            .add("bar")
            .end()
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .end()
            .end()
            .end()
            .end()
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
        .createForm()
        .submit();

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo.length()", Long.class)).isEqualTo(1);
    assertThat(submittedDocument.read("$.foo[0]", String.class)).isEqualTo("bar");
  }

  @Test
  @DisplayName("Pre selected options with maxItems <= 1 are implicitly submitted as single value")
  void test36() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .startObjectProperty("options")
            .put("maxItems", "1")
            .startArrayProperty("selectedValues")
            .add("bar")
            .end()
            .startArrayProperty("inline")
            .startObject()
            .put("value", "bar")
            .end()
            .end()
            .end()
            .end()
            .end()
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
        .createForm()
        .submit();

    DocumentContext submittedDocument = JsonPath.parse(myController.receivedPutBody);
    assertThat(submittedDocument.read("$.foo", String.class)).isEqualTo("bar");
  }

  @Test
  @DisplayName("Default value can be overriden")
  void test37() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .startObject()
            .put("name", "foo")
            .put("value", "bar")
            .put("required", true)
            .end()
            .end()
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
        .createForm()
        .withString("foo", "baz")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(JsonPath.parse(myController.receivedPutBody).read("$.foo", String.class))
        .isEqualTo("baz");
  }

  @Test
  @DisplayName("User can force pass an unknown property")
  void test38() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();

    assertThatCode(
            () ->
                form.withString(
                    "foo", "foo", PropertyValidationOption.Immediate.DO_NOT_FAIL_IF_NOT_DECLARED))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("User can force a readonly property")
  void test39() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
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

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();
    assertThatCode(
            () ->
                form.withString(
                    "foo",
                    "foo",
                    PropertyValidationOption.Immediate.DO_NOT_FAIL_IF_DECLARED_READ_ONLY))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("User can force an invalid property")
  void test40() throws Exception {
    myController.getResponseToSend =
        JSON.std
            .composeString()
            .startObject()
            .startObjectProperty("_links")
            .startObjectProperty("self")
            .put("href", "http://localhost/form-test:put")
            .end()
            .end()
            .startObjectProperty("_templates")
            .startObjectProperty("default")
            .put("method", "PUT")
            .startArrayProperty("properties")
            .end()
            .end()
            .end()
            .end()
            .finish();

    Form form =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).get()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .createForm();
    assertThatCode(() -> form.withString("foo", "foo", Immediate.DO_NOT_FAIL_IF_NOT_VALID))
        .doesNotThrowAnyException();
  }

  @Controller
  public static class MyController {

    private String getResponseToSend;
    private String receivedPutBody;
    private ResponseEntity<?> putResponseToSend;
    private ResponseEntity<?> getOptionsResponseToSend;

    void reset() {
      getResponseToSend = null;
      receivedPutBody = null;
      putResponseToSend = ResponseEntity.noContent().build();
      getOptionsResponseToSend = ResponseEntity.ok().body("[]");
    }

    @GetMapping(value = "form-test:get", produces = MediaTypes.HAL_FORMS_JSON_VALUE)
    public ResponseEntity<?> get() {
      return ResponseEntity.ok(getResponseToSend);
    }

    @PutMapping("form-test:put")
    public ResponseEntity<?> put(@RequestBody String body) {
      this.receivedPutBody = body;
      return putResponseToSend;
    }

    @GetMapping("form-test:get-options")
    public ResponseEntity<?> getOptions() {
      return getOptionsResponseToSend;
    }
  }
}
