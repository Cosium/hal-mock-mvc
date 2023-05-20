package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Réda Housni Alaoui
 */
public class Hop {

  private final String relationName;
  private final Map<String, Object> parameters = new HashMap<>();

  private Hop(String relationName) {
    this.relationName = requireNonNull(relationName);
  }

  public static Hop relation(String relation) {
    return new Hop(relation);
  }

  public Hop withParameter(String name, Object value) {
    parameters.put(name, value);
    return this;
  }

  public String relationName() {
    return relationName;
  }

  public Map<String, Object> parameters() {
    return parameters;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Hop.class.getSimpleName() + "[", "]")
        .add("relationName='" + relationName + "'")
        .add("parameters=" + parameters)
        .toString();
  }
}
