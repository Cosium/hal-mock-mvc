package com.cosium.hal_mock_mvc;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;

/**
 * @author Réda Housni Alaoui
 */
record ValidatedFormProperty<T>(
    FormProperty<T> property, @Nullable ValidationError firstValidationError) {

  public static <T> ValidatedFormProperty<T> markAsValid(FormProperty<T> formProperty) {
    return new ValidatedFormProperty<>(formProperty, null);
  }

  public static <T> DeferrableToServerSideValidationBuilder<T> invalidBuilder(
      FormProperty<T> formProperty) {
    return deferrableToServerSideValidation ->
        errorMessage ->
            new ValidatedFormProperty<>(
                formProperty, new ValidationError(errorMessage, deferrableToServerSideValidation));
  }

  public ValidatedFormProperty<T> populateRequestPayload(Map<String, Object> requestPayload) {
    property.populateRequestPayload(requestPayload);
    return this;
  }

  public Optional<String> serverSideVerifiableErrorMessage() {
    return Optional.ofNullable(firstValidationError)
        .filter(ValidationError::serverSideVerifiable)
        .map(ValidationError::reason);
  }

  public <S> ValidatedFormProperty<S> mapTo(FormProperty<S> formProperty) {
    return new ValidatedFormProperty<>(formProperty, firstValidationError);
  }

  record ValidationError(String reason, boolean serverSideVerifiable) {
    ValidationError {
      requireNonNull(reason);
    }
  }

  interface DeferrableToServerSideValidationBuilder<T> {
    ErrorMessageBuilder<T> serverSideVerifiable(boolean serverSideVerifiable);
  }

  interface ErrorMessageBuilder<T> {
    ValidatedFormProperty<T> reason(String reason);
  }
}
