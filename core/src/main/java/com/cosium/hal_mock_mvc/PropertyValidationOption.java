package com.cosium.hal_mock_mvc;

/**
 * @author Sébastien Le Ray
 * @author Réda Housni Alaoui
 */
public sealed interface PropertyValidationOption permits PropertyValidationOption.Immediate {
  enum Immediate implements PropertyValidationOption {
    /** Do not fail if writing to a property not declared by the template */
    DO_NOT_FAIL_IF_NOT_DECLARED,
    /** Do not fail if writing to a property declared as read-only by the template */
    DO_NOT_FAIL_IF_DECLARED_READ_ONLY
  }
}
