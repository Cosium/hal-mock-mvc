[![Build Status](https://github.com/Cosium/hal-mock-mvc/actions/workflows/ci.yml/badge.svg)](https://github.com/Cosium/hal-mock-mvc/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.cosium.hal_mock_mvc/hal-mock-mvc-spring-boot-starter.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.cosium.hal_mock_mvc%22%20AND%20a%3A%22hal-mock-mvc-spring-boot-starter%22)

# HAL Mock MVC

MockMvc wrapper allowing to easily test [Spring HATEOAS](https://github.com/spring-projects/spring-hateoas) HAL(-FORMS) endpoints.

# Quick start

1. Add the `spring-boot-starter` dependency:
    ```xml
    <dependency>
       <groupId>com.cosium.hal_mock_mvc</groupId>
       <artifactId>hal-mock-mvc-spring-boot-starter</artifactId>
       <version>${hal-mock-mvc.version}</version>
       <scope>test</scope>
    </dependency>
    ```
2. Annotate your test class with `AutoConfigureHalMockMvc` and inject `HalMockMvc`:
   ```java
   @AutoConfigureHalMockMvc
   @SpringBootTest
   class MyTest {
     @Autowired
     private HalMockMvc halMockMvc;
   
     @Test
     void test() {
       halMockMvc
          .follow("current-user")
          .get()
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.alias").value("jdoe"));
     }
   }
   ```

# Prerequisites

- Java 17+
- Spring dependencies matching Spring Boot 3 and above.

# Genesis

This project was created following https://github.com/spring-projects/spring-hateoas/issues/733 discussion.
