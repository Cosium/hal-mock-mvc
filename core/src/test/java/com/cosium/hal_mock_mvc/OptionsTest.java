package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.cosium.hal_mock_mvc.template.options.MapInlineElementRepresentation;
import com.cosium.hal_mock_mvc.template.options.OptionsLinkRepresentation;
import com.cosium.hal_mock_mvc.template.options.OptionsRepresentation;
import com.cosium.hal_mock_mvc.template.options.StringInlineElementRepresentation;
import com.fasterxml.jackson.jr.ob.JSON;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Réda Housni Alaoui
 */
@HalMockMvcBootTest
class OptionsTest {

  @Inject private MockMvc mockMvc;

  @Test
  void inlineOfStringArray() throws Exception {

    OptionsRepresentation options =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).getInlineOfStringArrays()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .representation()
            .propertyByName()
            .get("shipping")
            .options()
            .orElseThrow();

    assertThat(options.link()).isEmpty();
    assertThat(options.minItems()).isZero();
    assertThat(options.maxItems()).isEmpty();
    assertThat(options.promptField()).isEmpty();
    assertThat(options.valueField()).isEmpty();

    assertThat(options.selectedValues()).containsExactly("FedEx");
    assertThat(options.inline().orElseThrow())
        .allMatch(StringInlineElementRepresentation.class::isInstance)
        .map(StringInlineElementRepresentation.class::cast)
        .map(StringInlineElementRepresentation::value)
        .containsExactly("FedEx", "UPS", "DHL");
  }

  @Test
  void referenceFields() throws Exception {
    OptionsRepresentation options =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).getReferenceFields()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .representation()
            .propertyByName()
            .get("shipping")
            .options()
            .orElseThrow();

    assertThat(options.link()).isEmpty();
    assertThat(options.minItems()).isZero();
    assertThat(options.maxItems()).isEmpty();
    assertThat(options.promptField()).contains("shipName");
    assertThat(options.valueField()).contains("shipCode");

    assertThat(options.selectedValues()).containsExactly("FedEx");
    assertThat(options.inline().orElseThrow())
        .allMatch(MapInlineElementRepresentation.class::isInstance)
        .map(MapInlineElementRepresentation.class::cast)
        .map(MapInlineElementRepresentation::map)
        .contains(
            Map.of("shipName", "Federal Express", "shipCode", "FedEx"),
            Map.of("shipName", "United Parcel Service", "shipCode", "UPS"),
            Map.of("shipName", "DHL Express", "shipCode", "DHL"));
  }

  @Test
  void multipleReturnValues() throws Exception {
    OptionsRepresentation options =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).getMultipleReturnValues()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .representation()
            .propertyByName()
            .get("shipping")
            .options()
            .orElseThrow();

    assertThat(options.link()).isEmpty();
    assertThat(options.minItems()).isOne();
    assertThat(options.maxItems()).contains(2L);
    assertThat(options.promptField()).contains("shipName");
    assertThat(options.valueField()).contains("shipCode");

    assertThat(options.selectedValues()).containsExactly("FedEx");
    assertThat(options.inline().orElseThrow())
        .allMatch(MapInlineElementRepresentation.class::isInstance)
        .map(MapInlineElementRepresentation.class::cast)
        .map(MapInlineElementRepresentation::map)
        .contains(
            Map.of("shipName", "Federal Express", "shipCode", "FedEx"),
            Map.of("shipName", "United Parcel Service", "shipCode", "UPS"),
            Map.of("shipName", "DHL Express", "shipCode", "DHL"));
  }

  @Test
  void externalArrayOfValues() throws Exception {
    OptionsRepresentation options =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).getExternalArrayOfValues()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("default")
            .representation()
            .propertyByName()
            .get("shipping")
            .options()
            .orElseThrow();

    assertThat(options.minItems()).isZero();
    assertThat(options.maxItems()).isEmpty();
    assertThat(options.promptField()).isEmpty();
    assertThat(options.valueField()).isEmpty();

    assertThat(options.selectedValues()).containsExactly("FedEx");
    OptionsLinkRepresentation link = options.link().orElseThrow();
    assertThat(link.href()).isEqualTo("http://api.examples.org/shipping-options");
    assertThat(link.templated()).isFalse();
    assertThat(link.type()).contains("application/json");
  }

  @Controller
  public static class MyController {

    @GetMapping("options-test-affordance:inline-of-string-arrays")
    public ResponseEntity<?> getInlineOfStringArrays() throws IOException {

      String json =
          JSON.std
              .composeString()
              .startObject()
              .startObjectField("_links")
              .startObjectField("self")
              .put("href", "https://api.example.org/")
              .end()
              .end()
              .startObjectField("_templates")
              .startObjectField("default")
              .put("method", "POST")
              .startArrayField("properties")
              .startObject()
              .put("name", "shipping")
              .startObjectField("options")
              .startArrayField("selectedValues")
              .add("FedEx")
              .end()
              .startArrayField("inline")
              .add("FedEx")
              .add("UPS")
              .add("DHL")
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .finish();
      return ResponseEntity.ok(json);
    }

    @GetMapping("options-test-affordance:reference-fields")
    public ResponseEntity<?> getReferenceFields() throws IOException {

      String json =
          JSON.std
              .composeString()
              .startObject()
              .startObjectField("_links")
              .startObjectField("self")
              .put("href", "https://api.example.org/")
              .end()
              .end()
              .startObjectField("_templates")
              .startObjectField("default")
              .put("method", "POST")
              .startArrayField("properties")
              .startObject()
              .put("name", "shipping")
              .startObjectField("options")
              .startArrayField("selectedValues")
              .add("FedEx")
              .end()
              .startArrayField("inline")
              .startObject()
              .put("shipName", "Federal Express")
              .put("shipCode", "FedEx")
              .end()
              .startObject()
              .put("shipName", "United Parcel Service")
              .put("shipCode", "UPS")
              .end()
              .startObject()
              .put("shipName", "DHL Express")
              .put("shipCode", "DHL")
              .end()
              .end()
              .put("promptField", "shipName")
              .put("valueField", "shipCode")
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .finish();
      return ResponseEntity.ok(json);
    }

    @GetMapping("options-test-affordance:multiple-return-values")
    public ResponseEntity<?> getMultipleReturnValues() throws IOException {

      String json =
          JSON.std
              .composeString()
              .startObject()
              .startObjectField("_links")
              .startObjectField("self")
              .put("href", "https://api.example.org/")
              .end()
              .end()
              .startObjectField("_templates")
              .startObjectField("default")
              .put("method", "POST")
              .startArrayField("properties")
              .startObject()
              .put("name", "shipping")
              .startObjectField("options")
              .startArrayField("selectedValues")
              .add("FedEx")
              .end()
              .startArrayField("inline")
              .startObject()
              .put("shipName", "Federal Express")
              .put("shipCode", "FedEx")
              .end()
              .startObject()
              .put("shipName", "United Parcel Service")
              .put("shipCode", "UPS")
              .end()
              .startObject()
              .put("shipName", "DHL Express")
              .put("shipCode", "DHL")
              .end()
              .end()
              .put("minItems", 1)
              .put("maxItems", 2)
              .put("promptField", "shipName")
              .put("valueField", "shipCode")
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .finish();
      return ResponseEntity.ok(json);
    }

    @GetMapping("options-test-affordance:external-array-of-values")
    public ResponseEntity<?> getExternalArrayOfValues() throws IOException {
      String json =
          JSON.std
              .composeString()
              .startObject()
              .startObjectField("_links")
              .startObjectField("self")
              .put("href", "https://api.example.org/")
              .end()
              .end()
              .startObjectField("_templates")
              .startObjectField("default")
              .put("method", "POST")
              .startArrayField("properties")
              .startObject()
              .put("name", "shipping")
              .startObjectField("options")
              .startArrayField("selectedValues")
              .add("FedEx")
              .end()
              .startObjectField("link")
              .put("href", "http://api.examples.org/shipping-options")
              .put("templated", false)
              .put("type", "application/json")
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .end()
              .finish();
      return ResponseEntity.ok(json);
    }
  }

  @RequestMapping("/void")
  public abstract static class VoidAffordance {

    public static Affordance create() {
      return WebMvcLinkBuilder.afford(methodOn(HalMockMvcFormsTest.VoidAffordance.class).post());
    }

    @PostMapping
    public ResponseEntity<?> post() {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
  }
}
