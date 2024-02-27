package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Réda Housni Alaoui
 */
record FormProperty<T>(Class<T> valueType, String name, List<T> values, boolean array) {

  FormProperty {
    if (!array && values.size() > 1) {
      throw new IllegalArgumentException("Non array property can't hold more than 1 value.");
    }
    requireNonNull(valueType);
    requireNonNull(name);
    values = values.stream().filter(Objects::nonNull).toList();
  }

  public void populateRequestPayload(Map<String, Object> requestPayload) {
    Object value;
    if (array) {
      value = values;
    } else {
      value = values.stream().findFirst().orElse(null);
    }
    requestPayload.put(name, value);
  }

  public boolean isNumberValueType() {
    return Set.of(Integer.class, Long.class, Double.class).contains(valueType);
  }

  public List<Double> toDoubleValues() {
    if (!isNumberValueType()) {
      throw new IllegalArgumentException("%s is not a number type".formatted(valueType));
    }
    if (Integer.class.equals(valueType)) {
      return values.stream()
          .map(Integer.class::cast)
          .map(
              integer -> {
                if (integer == null) {
                  return null;
                }
                return integer.doubleValue();
              })
          .toList();
    } else if (Long.class.equals(valueType)) {
      return values.stream()
          .map(Long.class::cast)
          .map(
              aLong -> {
                if (aLong == null) {
                  return null;
                }
                return aLong.doubleValue();
              })
          .toList();
    } else if (Double.class.equals(valueType)) {
      return values.stream().map(Double.class::cast).toList();
    } else {
      throw new IllegalArgumentException("Unexpected value type %s".formatted(valueType));
    }
  }
}
