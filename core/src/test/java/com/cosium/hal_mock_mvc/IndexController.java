package com.cosium.hal_mock_mvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Réda Housni Alaoui
 */
@Controller
@RequestMapping("/")
public class IndexController {

  private static final String attribute = UUID.randomUUID().toString();

  @GetMapping
  public ResponseEntity<?> get() {
    return ResponseEntity.ok(Map.of(attribute, true));
  }

  public static ResultMatcher matchRootResource() {
    return jsonPath("$." + attribute).value(true);
  }
}
