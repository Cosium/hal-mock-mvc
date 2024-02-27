package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author Réda Housni Alaoui
 */
public class Form {

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final Template template;

  private final Map<String, ValidatedFormProperty<?>> propertyByName = new HashMap<>();

  Form(RequestExecutor requestExecutor, ObjectMapper objectMapper, Template template) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.objectMapper = requireNonNull(objectMapper);
    this.template = requireNonNull(template);
  }

  public Form withString(String propertyName, String value) throws Exception {
    FormProperty<?> property =
        new FormProperty<>(
            String.class, propertyName, Optional.ofNullable(value).stream().toList(), false);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withBoolean(String propertyName, Boolean value) throws Exception {
    FormProperty<?> property =
        new FormProperty<>(
            Boolean.class, propertyName, Optional.ofNullable(value).stream().toList(), false);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withInteger(String propertyName, Integer value) throws Exception {
    FormProperty<?> property =
        new FormProperty<>(
            Integer.class, propertyName, Optional.ofNullable(value).stream().toList(), false);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withLong(String propertyName, Long value) throws Exception {
    FormProperty<?> property =
        new FormProperty<>(
            Long.class, propertyName, Optional.ofNullable(value).stream().toList(), false);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withDouble(String propertyName, Double value) throws Exception {
    FormProperty<?> property =
        new FormProperty<>(
            Double.class, propertyName, Optional.ofNullable(value).stream().toList(), false);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withStrings(String propertyName, List<String> value) throws Exception {
    FormProperty<?> property = new FormProperty<>(String.class, propertyName, value, true);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withBooleans(String propertyName, List<Boolean> value) throws Exception {
    FormProperty<?> property = new FormProperty<>(Boolean.class, propertyName, value, true);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withIntegers(String propertyName, List<Integer> value) throws Exception {
    FormProperty<?> property = new FormProperty<>(Integer.class, propertyName, value, true);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withLongs(String propertyName, List<Long> value) throws Exception {
    FormProperty<?> property = new FormProperty<>(Long.class, propertyName, value, true);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public Form withDoubles(String propertyName, List<Double> value) throws Exception {
    FormProperty<?> property = new FormProperty<>(Double.class, propertyName, value, true);
    propertyByName.put(property.name(), validate(property));
    return this;
  }

  public ResultActions submit() throws Exception {
    String contentType = template.representation().contentType();
    if (!MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
      throw new UnsupportedOperationException(
          "Expected content type is '%s'. For now, the only supported content type is '%s'."
              .formatted(contentType, MediaType.APPLICATION_JSON_VALUE));
    }

    List<TemplateProperty> templateProperties =
        template.representation().propertyByName().values().stream()
            .map(
                propertyRepresentation ->
                    new TemplateProperty(requestExecutor, objectMapper, propertyRepresentation))
            .toList();

    Map<String, Object> payload = new HashMap<>();

    templateProperties.forEach(
        property -> {
          Object defaultValue = property.defaultValue().orElse(null);
          if (defaultValue == null) {
            return;
          }
          payload.put(property.name(), defaultValue);
        });

    List<String> expectedBadRequestReasons = new ArrayList<>();

    propertyByName
        .values()
        .forEach(
            formProperty ->
                formProperty
                    .populateRequestPayload(payload)
                    .serverSideVerifiableErrorMessage()
                    .ifPresent(expectedBadRequestReasons::add));

    templateProperties.stream()
        .filter(TemplateProperty::isRequired)
        .map(TemplateProperty::name)
        .filter(propertyName -> payload.get(propertyName) == null)
        .findFirst()
        .map("Property '%s' is required but is missing"::formatted)
        .ifPresent(expectedBadRequestReasons::add);

    ResultActions resultActions = template.submit(objectMapper.writeValueAsString(payload));
    if (expectedBadRequestReasons.isEmpty()) {
      return resultActions;
    }
    int status = resultActions.andReturn().getResponse().getStatus();
    HttpStatus.Series statusSeries = HttpStatus.Series.resolve(status);
    if (statusSeries == HttpStatus.Series.CLIENT_ERROR) {
      return resultActions;
    }
    throw new AssertionError(
        "An http status code 400 was expected because of the following reasons: [%s]. Got http status code %s instead."
            .formatted(String.join(",", expectedBadRequestReasons), status));
  }

  private ValidatedFormProperty<?> validate(FormProperty<?> property) throws Exception {
    TemplatePropertyRepresentation representation = requireTemplate(property);
    if (representation.readOnly()) {
      throw new AssertionError(
          "Cannot set value for read-only property '%s'".formatted(property.name()));
    }
    ValidatedFormProperty<?> validatedFormProperty =
        new TemplateProperty(requestExecutor, objectMapper, representation).validate(property);
    ValidatedFormProperty.ValidationError firstValidationError =
        validatedFormProperty.firstValidationError();
    if (firstValidationError != null && !firstValidationError.serverSideVerifiable()) {
      throw new AssertionError(firstValidationError.reason());
    }
    return validatedFormProperty;
  }

  private TemplatePropertyRepresentation requireTemplate(FormProperty<?> property) {
    TemplateRepresentation templateRepresentation = template.representation();
    return Optional.ofNullable(templateRepresentation.propertyByName().get(property.name()))
        .orElseThrow(
            () -> new AssertionError("No property '%s' found.".formatted(property.name())));
  }
}
