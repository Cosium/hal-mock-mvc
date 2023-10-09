package com.cosium.hal_mock_mvc;

/**
 * @author Réda Housni Alaoui
 */
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
  public void match(MvcResult result) throws Exception {
    Exception exception = null;
    for (ResultMatcher matcher : matchers) {
      try {
        matcher.match(result);
        return;
      } catch (Exception e) {
        if (exception == null) {
          exception = new Exception();
        }
        exception.addSuppressed(e);
      }
    }
    if (exception == null) {
      exception = new Exception();
    }
    throw exception;
  }
}
