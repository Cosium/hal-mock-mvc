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

# Usage

## Following HAL links

Follow a single relation from the base URI:

```java
halMockMvc
    .follow("users")
    .get()
    .andExpect(status().isOk());
```

Chain multiple hops to traverse deeper:

```java
halMockMvc
    .follow("users", "first")
    .get()
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("jdoe"));
```

Use `Hop` with URI template parameters:

```java
halMockMvc
    .follow(Hop.relation("file").withParameter("id", "foo"))
    .get()
    .andExpect(status().isOk());
```

## HTTP methods

Shorthand methods are available directly on the traversal builder:

```java
// GET
halMockMvc.follow("users").get()
    .andExpect(status().isOk());

// POST with JSON body
halMockMvc.follow("users").post("{\"name\":\"john\"}")
    .andExpect(status().isCreated());

// PUT with JSON body
halMockMvc.follow("user").put("{\"name\":\"jane\"}")
    .andExpect(status().isNoContent());

// PATCH with JSON body
halMockMvc.follow("user").patch("{\"name\":\"jane\"}")
    .andExpect(status().isNoContent());

// DELETE
halMockMvc.follow("user").delete()
    .andExpect(status().isNoContent());
```

For finer control, use the `request()` builder:

```java
halMockMvc.follow("users")
    .request()
    .jsonContent("{\"name\":\"john\"}")
    .post()
    .andExpect(status().isCreated());
```

## HAL-FORMS templates

Discover a template by key and submit it with raw JSON:

```java
halMockMvc
    .follow()
    .templates()
    .byKey("create")
    .submit("{\"name\":\"john\"}")
    .andExpect(status().isCreated());
```

Submit a template with no body (e.g. DELETE affordance):

```java
halMockMvc
    .follow()
    .templates()
    .byKey("deleteByName")
    .submit()
    .andExpect(status().isNoContent());
```

List all available templates:

```java
Collection<Template> templates = halMockMvc
    .follow()
    .templates()
    .list();

// Each Template exposes key() and representation()
// TemplateRepresentation exposes method(), contentType(), and target()
```

## Form builder

Use `createForm()` on a template for typed, validated form population:

```java
halMockMvc
    .follow()
    .templates()
    .byKey("create")
    .createForm()
    .withString("name", "john")
    .withInteger("age", 30)
    .withBoolean("active", true)
    .submit()
    .andExpect(status().isCreated());
```

Available typed methods: `withString`, `withBoolean`, `withInteger`, `withLong`, `withDouble`.
Collection variants: `withStrings`, `withBooleans`, `withIntegers`, `withLongs`, `withDoubles`.

## Create and shift

`createAndShift()` submits, expects a `201 Created` response, then starts a new traversal from the `Location` header:

```java
halMockMvc
    .follow()
    .templates()
    .byKey("create")
    .createAndShift("{\"name\":\"john\"}")
    .follow()
    .get()
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("john"));
```

`submitAndExpect204NoContent()` submits, expects `204 No Content`, then resumes the traversal. This is useful for update-then-read flows:

```java
halMockMvc
    .follow()
    .templates()
    .byKey("create")
    .createAndShift("{\"name\":\"john\"}")
    .follow()
    .templates()
    .byKey("changeCity")
    .submitAndExpect204NoContent("{\"city\":\"Casablanca\"}")
    .follow()
    .get()
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.value").value("Casablanca"));
```

Both methods are also available on the form builder (`Form#createAndShift()`, `Form#submitAndExpectNoContent()`).

## Multipart requests

### Direct multipart

Use `multipartRequest()` on the traversal builder:

```java
byte[] fileContent = "hello".getBytes(StandardCharsets.UTF_8);
halMockMvc
    .follow()
    .multipartRequest()
    .file("file", fileContent)
    .put()
    .andExpect(status().isNoContent());
```

### Template-based multipart

Use `multipart()` on a template:

```java
halMockMvc
    .follow(Hop.relation("file").withParameter("id", "foo"))
    .templates()
    .byKey("uploadFile")
    .multipart()
    .file("file", new byte[]{0})
    .submit()
    .andExpect(status().isNoContent());
```

Template-based multipart also supports `createAndShift()`:

```java
halMockMvc
    .follow()
    .templates()
    .byKey("addFile")
    .multipart()
    .file("file", new byte[]{0})
    .createAndShift()
    .follow()
    .get()
    .andExpect(status().isOk());
```

## Request customization

### Request post-processors

Add `RequestPostProcessor` instances to the builder (e.g. for authentication):

```java
HalMockMvc.builder(mockMvc)
    .baseUri("/api")
    .addRequestPostProcessor(request -> {
        request.addHeader("Authorization", "Bearer my-token");
        return request;
    })
    .build();
```

### Custom headers

Set default headers on the builder:

```java
HalMockMvc.builder(mockMvc)
    .baseUri("/api")
    .header("X-Tenant-Id", "acme")
    .build();
```

## Builder customizer (Spring Boot starter)

When using the Spring Boot starter, register a `HalMockMvcBuilderCustomizer` bean to globally customize every `HalMockMvc` instance:

```java
@TestConfiguration
class MyHalMockMvcConfig {

    @Bean
    HalMockMvcBuilderCustomizer securityCustomizer() {
        return builder -> builder.addRequestPostProcessor(
            SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")
        );
    }
}
```

# Prerequisites

- Java 17+
- Spring dependencies matching Spring Boot 4 and above.

# Genesis

This project was created following https://github.com/spring-projects/spring-hateoas/issues/733 discussion.
