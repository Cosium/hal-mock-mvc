package com.cosium.hal_mock_mvc;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * @author Réda Housni Alaoui
 */
@EnableHypermediaSupport(type = {HAL_FORMS, HAL})
@SpringBootApplication
@ComponentScan(basePackages = "com.cosium.hal_mock_mvc")
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
