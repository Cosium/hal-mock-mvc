package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import com.cosium.hal_mock_mvc.template.options.OptionsRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Réda Housni Alaoui
 */
class TemplateProperty {

  private static final Set<String> STRING_HAL_FORMS_TYPES =
      Set.of(
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
          "color");

  private static final Set<String> NUMBER_HAL_FORMS_TYPES = Set.of("month", "week", "number");

  private final RequestExecutor requestExecutor;
  private final ObjectMapper objectMapper;
  private final TemplatePropertyRepresentation representation;

  TemplateProperty(
      RequestExecutor requestExecutor,
      ObjectMapper objectMapper,
      TemplatePropertyRepresentation representation) {
    this.requestExecutor = requireNonNull(requestExecutor);
    this.objectMapper = requireNonNull(objectMapper);
    this.representation = requireNonNull(representation);
  }

  public String name() {
    return representation.name();
  }

  public Optional<Object> defaultValue() {
    OptionsRepresentation options = representation.options().orElse(null);
    if (options == null) {
      return representation.value().map(Object.class::cast);
    }

    Long maxItems = options.maxItems().orElse(null);
    if (maxItems != null && maxItems <= 1) {
      return options.selectedValues().stream().findFirst().map(Object.class::cast);
    }
    return Optional.of(options.selectedValues())
        .filter(Predicate.not(List::isEmpty))
        .map(Object.class::cast);
  }

  public boolean isRequired() {
    return representation.required();
  }

  public <T> ValidatedFormProperty<T> validate(FormProperty<T> property) throws Exception {

    Object firstValue = property.values().stream().findFirst().orElse(null);
    if (representation.required() && firstValue == null) {
      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "Since property '%s' is required, it must hold at least one non-null value."
                  .formatted(property.name()));
    }

    OptionsRepresentation options = representation.options().orElse(null);
    if (options != null) {
      return new TemplateOptions(requestExecutor, objectMapper, options).validate(property);
    }

    if (property.array()) {
      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "Given property '%s' has no options attribute, it cannot hold an array of values."
                  .formatted(property.name()));
    }

    String type = representation.type();
    if (STRING_HAL_FORMS_TYPES.contains(type) && !String.class.equals(property.valueType())) {
      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "Value must be of type String because property '%s' has type '%s'"
                  .formatted(property.name(), type));
    }
    if (NUMBER_HAL_FORMS_TYPES.contains(type) && !property.isNumberValueType()) {
      return ValidatedFormProperty.invalidBuilder(property)
          .serverSideVerifiable(true)
          .reason(
              "Value must be of type Number because property '%s' has type '%s'"
                  .formatted(property.name(), type));
    }

    String regex = representation.regex().orElse(null);
    if (regex != null) {
      if (!String.class.equals(property.valueType())) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Property '%s' must have a value of type String because it is associated to a regex"
                    .formatted(property.name()));
      }
      String stringValue = (String) firstValue;
      if (firstValue != null && !Pattern.compile(regex).matcher(stringValue).matches()) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value '%s' of property '%s' does not match regex '%s'"
                    .formatted(stringValue, property.name(), regex));
      }
    }

    Double max = representation.max().orElse(null);
    if (max != null) {
      if (!property.isNumberValueType()) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value of property '%s' must be a number because the property defines a max value."
                    .formatted(property.name()));
      }
      Double doubleValue = property.toDoubleValues().stream().findFirst().orElse(null);
      if (doubleValue != null && doubleValue > max) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value %s is greater than the max value of %s of property '%s'."
                    .formatted(doubleValue, max, property.name()));
      }
    }

    Double min = representation.min().orElse(null);
    if (min != null) {
      if (!property.isNumberValueType()) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value of property '%s' must be a number because the property defines a min value."
                    .formatted(property.name()));
      }
      Double doubleValue = property.toDoubleValues().stream().findFirst().orElse(null);
      if (doubleValue != null && doubleValue < min) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value %s is lower than the min value of %s of property '%s'."
                    .formatted(doubleValue, min, property.name()));
      }
    }

    Long maxLength = representation.maxLength().orElse(null);
    if (maxLength != null) {
      if (!String.class.equals(property.valueType())) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value of property '%s' must be a string because the property defines a max length value."
                    .formatted(property.name()));
      }
      String stringValue = (String) firstValue;
      if (stringValue != null && stringValue.length() > maxLength) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value '%s' of property '%s' has a greater length than the defined max length of %s"
                    .formatted(stringValue, property.name(), maxLength));
      }
    }

    Long minLength = representation.minLength().orElse(null);
    if (minLength != null) {
      if (!String.class.equals(property.valueType())) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value of property '%s' must be a string because the property defines a min length value."
                    .formatted(property.name()));
      }
      String stringValue = (String) firstValue;
      if (stringValue != null && stringValue.length() < minLength) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value '%s' of property '%s' has a lower length than the defined min length of %s"
                    .formatted(stringValue, property.name(), minLength));
      }
    }

    Double step = representation.step().orElse(null);
    if (step != null) {
      if (!property.isNumberValueType()) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value of property '%s' must be a number because the property defines a step value."
                    .formatted(property.name()));
      }
      Double doubleValue = property.toDoubleValues().stream().findFirst().orElse(null);
      if (doubleValue != null && Math.round(doubleValue / step) != doubleValue / step) {
        return ValidatedFormProperty.invalidBuilder(property)
            .serverSideVerifiable(true)
            .reason(
                "Value '%s' of property '%s' is not compatible with the defined step of '%s'"
                    .formatted(doubleValue, property.name(), step));
      }
    }

    return ValidatedFormProperty.markAsValid(property);
  }
}
