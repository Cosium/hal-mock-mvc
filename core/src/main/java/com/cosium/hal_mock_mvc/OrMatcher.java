package com.cosium.hal_mock_mvc;

/**
 * @author Réda Housni Alaoui
 */
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * @author Réda Housni Alaoui
 */
class OrMatcher implements ResultMatcher {

  private final List<ResultMatcher> matchers;

  private OrMatcher(List<ResultMatcher> matchers) {
    this.matchers = matchers;
  }

  public static ResultMatcher anyOf(ResultMatcher... matchers) {
    return new OrMatcher(List.of(matchers));
  }

  @Override
  public void match(MvcResult result) {
    List<Throwable> failures = new ArrayList<>();
    for (ResultMatcher matcher : matchers) {
      try {
        matcher.match(result);
        return;
      } catch (Exception | AssertionError e) {
        failures.add(e);
      }
    }

    AssertionError error =
        new AssertionError(
            failures.stream()
                .map(Throwable::getMessage)
                .collect(joining("\n- ", "None of the matchers matched:\n- ", "")));
    failures.forEach(error::addSuppressed);
    throw error;
  }
}
