package com.cosium.hal_mock_mvc_spring_boot_starter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation that can be applied to a test class to enable and configure auto-configuration of
 * {@link com.cosium.hal_mock_mvc.HalMockMvc}.
 *
 * @author Réda Housni Alaoui
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigureMockMvc
public @interface AutoConfigureHalMockMvc {

  /**
   * @see AutoConfigureMockMvc#addFilters()
   */
  @AliasFor(annotation = AutoConfigureMockMvc.class, attribute = "addFilters")
  boolean addFilters() default true;

  /**
   * @see AutoConfigureMockMvc#print()
   */
  @AliasFor(annotation = AutoConfigureMockMvc.class, attribute = "print")
  MockMvcPrint print() default MockMvcPrint.DEFAULT;

  /**
   * @see AutoConfigureMockMvc#printOnlyOnFailure()
   */
  @AliasFor(annotation = AutoConfigureMockMvc.class, attribute = "printOnlyOnFailure")
  boolean printOnlyOnFailure() default true;

  /**
   * @see AutoConfigureMockMvc#htmlUnit()
   */
  @AliasFor(annotation = AutoConfigureMockMvc.class, attribute = "htmlUnit")
  AutoConfigureMockMvc.HtmlUnit htmlUnit() default @AutoConfigureMockMvc.HtmlUnit;
}
